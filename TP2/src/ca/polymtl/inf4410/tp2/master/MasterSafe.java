package ca.polymtl.inf4410.tp2.master;

import ca.polymtl.inf4410.tp2.shared.Operation;
import ca.polymtl.inf4410.tp2.shared.RequestRejectedException;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alexandre on 11/14/2015.
 */
public class MasterSafe extends Master {

    public MasterSafe(MasterConfig config) throws IOException {
        super(config);
        startMaster = System.nanoTime();
    }

    protected void dispatchWork() {
        int count = serverStubs.size();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Runner r = new RunnerSafe(i, config.getBatchSize(), serverStubs.get(i));
                r.start();
                runners.add(r);
            }
        } else {
            System.err.println("Could not connect to any worker. The process will end now");
        }
    }

    protected void completeRunnerExecution(int index) {

        // Check if last to finish
        // This should be synchronized somehow
        boolean last = true;

        for (int i = 0; i < runners.size(); i++) {
        	 if (!runners.get(i).terminated) {
                 last = false;
                 break;
             }
        }

        if (last) {
            System.out.println("Last runner computing final result");

        	for(int i = 0; i < runners.size(); ++i)
        	{
                System.out.println("Runner " + i + " had a result of " + runners.get(i).result.get());
        		result.addAndGet(runners.get(i).result.get());
        	}

            int total = result.get() % 5000;
            stopMaster = System.nanoTime();
            System.out.println("Result is " + total + "took " +  (stopMaster - startMaster) / 1000000 + " ms");
            
        }
    }

    protected void alertWorkerDisconnected(int index) {
        // Redispatch evenly
        List<Runner> remainingRunners = new ArrayList<>();
        for (Runner runner : runners) {
            if (!runner.failed) {
                remainingRunners.add(runner);
            }
        }

        int count = remainingRunners.size();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Runner runner = remainingRunners.get(i);

                if (runner.terminated) {
                    System.out.println("Restarting runner " + i + " to take over remaining operations");
                    runner = new RunnerSafe(i, config.getBatchSize(), serverStubs.get(i));
                    runner.start();
                }
            }
        } else {
            // Crash and burn
            System.err.println("Last node just died. Cannot continue.");
        }
    }

    protected class RunnerSafe extends Runner {
        public RunnerSafe(int index, int size, ServerInterface worker) {
            super(index, size, worker);
        }

        @Override
        public void run() {
            try {
                while (!operationQueue.isEmpty() || !pendingOperations.isEmpty()) {

                    if (pendingOperations.isEmpty()) {
                        for (int i = 0; i < size && !operationQueue.isEmpty(); i++) {
                            pendingOperations.add(operationQueue.poll());
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
                        result.updateAndGet(operand -> operand % 5000);

                        int batchSize = batch.size();
                        System.out.println("Worker " + index + " processed operations " + processedOps + " through " + (processedOps + batchSize - 1) + " (" + batchSize + " ops) - took " + (stop - start) / 1000000 + " ms - result : " +res);

                        processedOps += batchSize;
                        pendingOperations.clear();
                    } catch (RequestRejectedException e) {
                    	System.out.println("Operation rejected");
                        retryStrategy();
                    }

                }

                completeWork();

            } catch (RemoteException e) {
                e.printStackTrace();
                handleFailure();
            }
        }

        protected void completeWork() {
            System.out.println("Worker " + index + " completed work");
            terminated = true;
            completeRunnerExecution(index);
        }
    }

}
