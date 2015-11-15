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
public class MasterUnsafe extends Master {

	private int resultatTemp1 = 0;
	private int resultatTemp2 = 0;
	private int resultatTemp3 = 0;
	
    public MasterUnsafe(MasterConfig config) throws IOException {
        super(config);
    }

    protected void dispatchWork() {
        int count = serverStubs.size();

        for (int i = 0; i < count; i++) {
            Runner r = new RunnerUnsafe(i, operations, 100, serverStubs.get(i));
            r.start();
            runners.add(r);
        }
    }

    protected void completeRunnerExecution(int index) {
    	
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
        	
        	for (int i = 0; i < runners.size(); ++i) {
        		if(i == 0)
                {
                	resultatTemp1 = runners.get(i).result.get();
                }
                else if(i == 1)
                {
                	resultatTemp2 = runners.get(i).result.get();
                }
                else if(i == 2)
                {
                	resultatTemp3 = runners.get(i).result.get();
                }
        	}
        	if( resultatTemp1 != resultatTemp2 && resultatTemp1 != resultatTemp3  && resultatTemp2 != resultatTemp3 )
        	{
        		// RERUN
        	}
        	
        	if(resultatTemp1 == resultatTemp2)
             	result.set(resultatTemp1);
             else if(resultatTemp1 == resultatTemp3)
            	 result.set(resultatTemp1);
             else if(resultatTemp2 == resultatTemp3)
            	 result.set(resultatTemp2);
        	
        	
            System.out.println("Result is " + result);
        }
    }

    protected void alertWorkerDisconnected(int index, Operation[] remainingOp) {
        // just give it all to another one
        int next = (index + 1) % serverStubs.size();

        if (next != index) {
            runners.get(next).addOperations(remainingOp);
        } else {
            // Crash and burn
            System.out.println("Last node just died.");
        }
    }

    protected class RunnerUnsafe extends Runner {
        public RunnerUnsafe(int index, List<Operation> operationPool, int size, ServerInterface worker) {
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
                        // Retry
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