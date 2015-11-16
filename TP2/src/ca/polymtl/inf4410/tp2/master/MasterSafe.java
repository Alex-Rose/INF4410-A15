package ca.polymtl.inf4410.tp2.master;

import ca.polymtl.inf4410.tp2.operations.Operations;
import ca.polymtl.inf4410.tp2.shared.Operation;
import ca.polymtl.inf4410.tp2.shared.RequestRejectedException;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexandre on 11/14/2015.
 */
public class MasterSafe extends Master {

    public MasterSafe(MasterConfig config) throws IOException {
        super(config);
    }

    protected void dispatchWork() {
        int count = serverStubs.size();

        if (count > 0) {
            int size = operations.size() / count;

            for (int i = 0; i < count; i++) {
                int batchSize = i == count - 1 ? size + operations.size() % count : size;

                int begin = i * size;
                int end = begin + batchSize;
                Runner r = new RunnerSafe(i, operations.subList(i * size, end), 10, serverStubs.get(i));
                r.start();
                runners.add(r);
            }
        } else {
            System.err.println("Could not connect to any worker. The process will end now");
        }
    }

    protected void completeRunnerExecution(int index) {
        //result.addAndGet(runners.get(index).result.get());
       // result.updateAndGet(operand -> operand % 5000);

        // Check if last to finish
        // This should be synchronized somehow
        boolean last = true;
 /*       for (int i = 0; i < runners.size(); i++) {
            if (index == i) continue;

            if (!runners.get(i).terminated) {
                last = false;
                break;
            }
        }*/
        for (int i = 0; i < runners.size(); i++) {
        	 if (!runners.get(i).terminated) {
                 last = false;
                 break;
             }
        }

        if (last) {
        	int total = 0;
        	for(int i = 0; i < runners.size(); ++i)
        	{
        		total += runners.get(i).result.get();
        	}
            //System.out.println("Result is " + result);
            System.out.println("Result is " + total%5000);
        }
    }

    protected void alertWorkerDisconnected(int index, Operation[] remainingOp) {
        // just give it all to another one
        int next = (index + 1) % serverStubs.size();
        
        if (next != index) {
        	List<Operation> list = new ArrayList<Operation>(Arrays.asList(remainingOp));
        	Runner r = new RunnerSafe(runners.size(), list, 100, serverStubs.get(next));
            r.start();
            runners.add(r);
        } else {
            // Crash and burn
            System.out.println("Last node just died.");
        }
    }

    protected class RunnerSafe extends Runner {
        public RunnerSafe(int index, List<Operation> operationPool, int size, ServerInterface worker) {
            super(index, operationPool, size, worker);
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
                    	System.out.println("Operation rejected");
                    }
                }

                completeWork();

            } catch (RemoteException e) {
                e.printStackTrace();
                handleFailure();
            }
        }
        
        

        protected void completeWork() {
            terminated = true;
            completeRunnerExecution(index);
        }

        protected void handleFailure() {
            System.out.println("Worker " + index + " died");
            operationLock.lock();
            terminated = true;
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
