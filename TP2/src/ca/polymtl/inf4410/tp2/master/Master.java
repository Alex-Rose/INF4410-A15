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

public abstract class Master {
	protected ArrayList<ServerInterface> serverStubs;
    protected ArrayList<Runner> runners;
	protected ArrayList<Operation> operations;
    protected MasterConfig config;

    protected AtomicInteger result;

    abstract protected void dispatchWork();
    abstract protected void completeRunnerExecution(int index);
    abstract protected void alertWorkerDisconnected(int index, Operation[] remainingOp);

    public Master(MasterConfig config) throws IOException {
        result = new AtomicInteger();
        runners = new ArrayList<>();
        this.config = config;

        populateOperations();
         initializeServerStubs();
        dispatchWork();
    }

    protected void initializeServerStubs(){
		serverStubs = new ArrayList<ServerInterface>();
        ArrayList<String> servers = config.getServers();
		for (int i = 0; i < servers.size(); i++){
			ServerInterface stub = null;
			try {
	            Registry registry = LocateRegistry.getRegistry(servers.get(i), config.getRmiPort());
	            stub = (ServerInterface) registry.lookup(config.getWorkerName());
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

    protected void populateOperations() throws IOException{
        operations = new ArrayList<Operation>();
        for (String line : Files.readAllLines(Paths.get(config.getOperationFile()))) {
            String[] splitLine = line.split("\\s+");
            Operation operation = new Operation(splitLine[0], Integer.parseInt(splitLine[1]));
            operations.add(operation);
        }
    }

    protected abstract class Runner extends Thread{

        public final int index;
        public ConcurrentLinkedQueue<Operation> operationPool;
        public ConcurrentLinkedQueue<Operation> pendingOperations;
        public ServerInterface worker;
        public int size;
        public AtomicInteger result;
        public boolean terminated;
        public int processedOps;

        public Lock operationLock;

        abstract protected void completeWork();

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

        protected void handleFailure() {
            System.out.println("Worker " + index + " died");
            operationLock.lock();

            while (pendingOperations.size() > 0) {
                operationPool.add(pendingOperations.poll());
            }

            operationLock.unlock();

            alertWorkerDisconnected(index, operationPool.toArray(new Operation[0]));
        }

        public void addOperations(Operation[] remainingOp) {
            operationLock.lock();
            operationPool.addAll(Arrays.asList(remainingOp));
            operationLock.unlock();
        }
    }


}
