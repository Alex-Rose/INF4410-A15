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

import ca.polymtl.inf4410.tp2.shared.*;

public class Master {
	private ArrayList<ServerInterface> serverStubs;
    private ArrayList<Runner> runners;
	private ArrayList<Operation> operations;
    private MasterConfig config;

    private AtomicInteger result;

    public Master(MasterConfig config) throws IOException {
        result = new AtomicInteger();
        runners = new ArrayList<>();
        this.config = config;

        populateOperations();
        initializeServerStubs();
        dispatchWork();
    }

    private void populateOperations() throws IOException{
		operations = new ArrayList<Operation>();
		for (String line : Files.readAllLines(Paths.get(config.getOperationFile()))) {
			String[] splitLine = line.split("\\s+");
			Operation operation = new Operation(splitLine[0], Integer.parseInt(splitLine[1]));
			operations.add(operation);
		}
	}
	
	private void initializeServerStubs(){
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

    private void dispatchWork() {
        int count = serverStubs.size();
        int size = operations.size() / count;

        for (int i = 0; i < count; i++) {
            int batchSize = i == count - 1 ? size + operations.size() % count : size;

            int begin = i * size;
            int end = begin + batchSize;
            Runner r = new Runner(i, operations.subList(i * size, end), 100, serverStubs.get(i));
            r.start();
            runners.add(r);
        }


    }

    private void completeRunnerExecution(int index) {
        result.addAndGet(runners.get(index).result.get());
        result.updateAndGet(operand -> operand % 5000);

        // Check if last to finish
        // This should be synchronized somehow
        boolean last = true;
        for (int i = 0; i < runners.size(); i++) {
            if (index == i) continue;

            if (!runners.get(i).terminated) {
                last = false;
                break;
            }
        }

        if (last) {
            System.out.println("Result is " + result);
        }
    }

    private void alertWorkerDisconnected(int index, Operation[] remainingOp) {
        // just give it all to another one
        int next = (index + 1) % serverStubs.size();

        if (next != index) {
            runners.get(next).addOperations(remainingOp);
        } else {
            // Crash and burn
            System.out.println("Last node just died.");
        }
    }

    private class Runner extends Thread {

        public final int index;
        public ConcurrentLinkedQueue<Operation> operationPool;
        public ConcurrentLinkedQueue<Operation> pendingOperations;
        public ServerInterface worker;
        public int size;
        public AtomicInteger result;
        public boolean terminated;
        public int processedOps;

        public Lock operationLock;

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

        @Override
        public void run() {
            try {
                while (!operationPool.isEmpty()) {

                    if (pendingOperations.isEmpty()) {
                        for (int i = 0; i < size && !operationPool.isEmpty(); i++) {
                            pendingOperations.add(operationPool.poll());
                        }
                    }

                    List<Operation> batch = new ArrayList<Operation>();

                    for (Iterator<Operation> it = pendingOperations.iterator(); it.hasNext(); ) {
                        batch.add(it.next());
                    }


                    try {
                        long start = System.nanoTime();
                        int res = worker.executeOperations(batch);
                        long stop = System.nanoTime();

                        result.addAndGet(res);

                        int batchSize = batch.size();
                        System.out.println("Worker " + index + " processed operations " + processedOps + " through " + (processedOps + batchSize - 1) + " (" + batchSize + " ops) - took " + (stop - start) / 1000000 + " ms - result : " +res);

                        processedOps += batchSize;
                        pendingOperations.clear();
                    } catch (RequestRejectedException e) {
                        // Retry
                    }
                }

                completeWork();

            } catch (RemoteException e) {
                e.printStackTrace();
                handleFailure();
            }
        }

        private void completeWork() {
            terminated = true;
            completeRunnerExecution(index);
        }

        private void handleFailure() {
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
