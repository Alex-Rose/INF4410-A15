package ca.polymtl.inf4410.tp2.master;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.polymtl.inf4410.tp2.operations.Operations;
import ca.polymtl.inf4410.tp2.shared.*;
import javafx.util.Pair;

import javax.net.ssl.HttpsURLConnection;

public abstract class Master {
	protected ArrayList<ServerInterface> serverStubs;
    protected ArrayList<Runner> runners;
	protected ArrayList<Operation> operations;
    protected ConcurrentLinkedQueue<Operation> operationQueue;
    protected MasterConfig config;
    protected long startMaster;
    protected long stopMaster;
    protected long totalTime;

    protected AtomicInteger result;

    /**
     * Dispatch operations between all workers.
     *
     * This has to be implemented and can be overridden by children
     * to change strategy
     */
    abstract protected void dispatchWork();

    /**
     * Work to be done by each worker when they complete
     *
     * This has to be implemented and can be overridden by children
     * to work with dispatch strategy
     */
    abstract protected void completeRunnerExecution(int index);

    /**
     * Work to be done when a worker disconnects
     *
     * This has to be implemented and can be overridden by children
     * to work with dispatch strategy
     */
    abstract protected void alertWorkerDisconnected(int index);

    /**
     * @param config configuration to be used with this instance
     * @throws IOException
     */
    public Master(MasterConfig config) throws IOException {
        result = new AtomicInteger();
        runners = new ArrayList<>();
        this.config = config;

        populateOperations();
        validateConfig();
        initializeServerStubs();
        dispatchWork();
    }

    private void validateConfig() {
        int max = operations.size() / config.getServers().size();
        if (config.getBatchSize() > max) {
            System.out.println("Batch size can't be more than [operations]/[workers]. Enforcing limit " + max);
            config.setBatchSize(max);
        }
    }

    /**
     * Initiate connections to remote workers
     */
    protected void initializeServerStubs(){
		serverStubs = new ArrayList<ServerInterface>();
        ArrayList<Pair<String, String>> servers = config.getServers();
		for (int i = 0; i < servers.size(); i++){
			ServerInterface stub = null;
			try {
	            Registry registry = LocateRegistry.getRegistry(servers.get(i).getKey(), config.getRmiPort());
	            stub = (ServerInterface) registry.lookup(servers.get(i).getValue());
                serverStubs.add(stub);
	        } catch (NotBoundException e) {
	            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
	        } catch (AccessException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        } catch (RemoteException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        }
		}
	}

    /**
     * Reads operations from file
     * @throws IOException
     */
    protected void populateOperations() throws IOException{
        operations = new ArrayList<Operation>();
        for (String line : Files.readAllLines(Paths.get(config.getOperationFile()))) {
            String[] splitLine = line.split("\\s+");
            Operation operation = new Operation(splitLine[0], Integer.parseInt(splitLine[1]));
            operations.add(operation);
        }

        operationQueue = new ConcurrentLinkedQueue<Operation>(operations);
    }

    //http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
    protected void saveResults() throws Exception {
        String url = "http://step.polymtl.ca/~alexrose/inf4410.php";
        URL obj = new URL(url);
//        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "java_truie/1.1");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "safe=" + (config.isSafeMode() ? "1" : "0");
        urlParameters += "&batch_size=" + config.getBatchSize();
        urlParameters += "&file=" + config.getOperationFile();
        urlParameters += "&batch_size=" + config.getBatchSize();
        urlParameters += "&servers=";
        for (Pair<String, String> s : config.getServers()) urlParameters += s.getKey() + ";";
        urlParameters += "&capacities=";
        for (ServerInterface s : serverStubs) urlParameters += s.getCapacity() + ";";
        urlParameters += "&malice=";
        for (ServerInterface s : serverStubs) urlParameters += s.getMaliceValue() + ";";
        urlParameters += "&result=" + result;
        urlParameters += "&time=" + totalTime;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + urlParameters);
//        System.out.println("Response Code : " + responseCode);
        if (responseCode == 200) {
            System.out.println("Results were saved");
        } else {
            System.out.println("Results could not be saved");
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }

    /**
     * Abstract class used to implement a Thread which will be responsible
     * for communicating with a single worker.
     */
    protected abstract class Runner extends Thread{

        public final int index;
        public ConcurrentLinkedQueue<Operation> pendingOperations;
        public ServerInterface worker;
        public int size;
        public AtomicInteger result;
        public boolean terminated;
        public boolean failed;
        public int processedOps;

        /**
         * Work to be done by each worker when they complete
         *
         * This has to be implemented and can be overridden by children
         * to work with dispacth strategy
         */
        abstract protected void completeWork();

        /**
         * @param index unique ID of this worker
         * @param size initial batch size to request
         * @param worker remote worker stub
         */
        public Runner(int index, int size, ServerInterface worker) {
            this.pendingOperations = new ConcurrentLinkedQueue<>();
            this.size = size;
            this.result = new AtomicInteger();
            this.result.set(0);
            this.processedOps = 0;
            this.size = size;
            this.index = index;
            this.worker = worker;
        }

        /**
         * Work to be done when a worker disconnects
         *
         * This is a default implementation but it can be overridden by children
         */
        protected void handleFailure() {
            this.failed = true;
            terminated = true;

            System.out.println("Worker " + index + " died");

            while (pendingOperations.size() > 0) {
                operationQueue.add(pendingOperations.poll());
            }

            alertWorkerDisconnected(index);
        }

        /**
         * Action taken when an operation is rejected.
         * Default is nothing: pending ops will be resent as is
         */
        protected void retryStrategy() {

        }
    }


}
