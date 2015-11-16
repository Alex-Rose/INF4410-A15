package ca.polymtl.inf4410.tp2.master;

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

public abstract class Master {
	protected ArrayList<ServerInterface> serverStubs;
    protected ArrayList<Runner> runners;
	protected ArrayList<Operation> operations;
    protected MasterConfig config;

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
    abstract protected void alertWorkerDisconnected(int index, Operation[] remainingOp);

    /**
     * @param config configuration to be used with this instance
     * @throws IOException
     */
    public Master(MasterConfig config) throws IOException {
        result = new AtomicInteger();
        runners = new ArrayList<>();
        this.config = config;

        populateOperations();
        initializeServerStubs();
        dispatchWork();
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
    }

    /**
     * Abstract class used to implement a Thread which will be responsible
     * for communicating with a single worker.
     */
    protected abstract class Runner extends Thread{

        public final int index;
        public ConcurrentLinkedQueue<Operation> operationPool;
        public ConcurrentLinkedQueue<Operation> pendingOperations;
        public ServerInterface worker;
        public int size;
        public AtomicInteger result;
        public boolean terminated;
        public boolean failed;
        public int processedOps;

        public Lock operationLock;

        /**
         * Work to be done by each worker when they complete
         *
         * This has to be implemented and can be overridden by children
         * to work with dispacth strategy
         */
        abstract protected void completeWork();

        /**
         * @param index unique ID of this worker
         * @param operationPool list of operations to execute
         * @param size initial batch size to request
         * @param worker remote worker stub
         */
        public Runner(int index, List<Operation> operationPool, int size, ServerInterface worker) {
            this.operationPool = new ConcurrentLinkedQueue<>(operationPool);
            this.pendingOperations = new ConcurrentLinkedQueue<>();
            this.operationLock = new ReentrantLock();
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

            System.out.println("Worker " + index + " died");
            operationLock.lock();

            while (pendingOperations.size() > 0) {
                operationPool.add(pendingOperations.poll());
            }

            operationLock.unlock();

            alertWorkerDisconnected(index, operationPool.toArray(new Operation[0]));
        }

        /**
         * Add new operations to the current queue
         *
         * @param newOps new operations to be added
         */
        public void addOperations(Operation[] newOps) {
            operationLock.lock();
            operationPool.addAll(Arrays.asList(newOps));
            operationLock.unlock();
        }

        /**
         * Action taken when an operation is rejected.
         * Default is nothing: pending ops will be resent as is
         */
        protected void retryStrategy() {

        }
    }


}
