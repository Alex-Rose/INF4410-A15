package ca.polymtl.inf4410.tp2.master;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

            Runner r = new Runner(i, operations.subList(i * size, batchSize), 10, serverStubs.get(i));
            runners.add(r);
            r.start();
        }


    }

    private void completeRunnerExecution(int index) {
        result.addAndGet(runners.get(index).result.get());

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

        public Lock operationLock;

        public Runner(int index, List<Operation> operationPool, int size, ServerInterface worker) {
            this.operationPool = new ConcurrentLinkedQueue<>(operationPool);
            this.size = size;
            result = new AtomicInteger();
            result.set(0);
            this.size = size;
            this.index = index;
            this.worker = worker;
        }

        @Override
        public void run() {

            while (!operationPool.isEmpty()) {

                List<Operation> batch = new ArrayList<>();

                for (int i = 0; i < size && !operationPool.isEmpty(); i++) {
                    batch.add(operationPool.poll());
                }

                try {
                    result.addAndGet(worker.executeOperations(batch));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            completeWork();
        }

        private void completeWork() {
            terminated = true;
            completeRunnerExecution(index);
        }

        private void handleFailure() {
            operationLock.lock();

            while (pendingOperations.size() > 0) {
                operationPool.add(pendingOperations.poll());
            }

            operationLock.unlock();

            alertWorkerDisconnected(index, (Operation[]) operationPool.toArray());
        }

        public void addOperations(Operation[] remainingOp) {
            operationLock.lock();
            operationPool.addAll(Arrays.asList(remainingOp));
            operationLock.unlock();
        }
    }
}
