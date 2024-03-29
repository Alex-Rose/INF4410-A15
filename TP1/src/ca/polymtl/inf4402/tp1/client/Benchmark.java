package ca.polymtl.inf4402.tp1.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by Alexandre on 9/15/2015.
 */
public class Benchmark {
    protected Benchmark(){
        try {
            String resultsDir = "results";
            File folder = new File(resultsDir);

            if (!folder.exists()){
                folder.mkdir();
            }

            long timestamp = System.currentTimeMillis() / 1000L;

            normalWriter = new Writer(fileNameBuilder(resultsDir, "normal", timestamp));
            normalWriter.start();
            localWriter = new Writer(fileNameBuilder(resultsDir, "local", timestamp));
            localWriter.start();
            remoteWriter = new Writer(fileNameBuilder(resultsDir, "remote", timestamp));
            remoteWriter.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static Benchmark instance = null;
    protected Writer normalWriter = null;
    protected Writer localWriter = null;
    protected Writer remoteWriter = null;

    // Data aggregation
    protected long normalTotal = 0;
    protected int normalCount = 0;
    protected long normalSize = 0;
    protected long localTotal = 0;
    protected int localCount = 0;
    protected long localSize = 0;
    protected long remoteTotal = 0;
    protected int remoteCount = 0;
    protected long remoteSize = 0;

    public static Benchmark getInstance(){
        if (instance == null){
            instance = new Benchmark();
        }

        return instance;
    }

    public void writeNormalAsync(Long time, int size, int result){
        normalWriter.put(new Result(time, size, result));

        normalTotal += time;
        normalCount += 1;
        normalSize += size;
    }

    public void writeLocalAsync(Long time, int size, int result){
        localWriter.put(new Result(time, size, result));

        localTotal += time;
        localCount += 1;
        localSize += size;
    }

    public void writeRemoteAsync(Long time, int size, int result){
        remoteWriter.put(new Result(time, size, result));

        remoteTotal += time;
        remoteCount += 1;
        remoteSize += size;
    }

    public Result getResult(String type){
        Result result = new Result();
        if (type.equals("normal")){
            result.size = (int)normalSize / normalCount;
            result.time = normalTotal / normalCount;
        }
        else if (type.equals("local")){
            result.size = (int)localSize / localCount;
            result.time = localTotal / localCount;
        }
        else if (type.equals("remote")){
            result.size = (int)remoteSize / remoteCount;
            result.time = remoteTotal / remoteCount;
        }

        return result;
    }

    public void flushAndDelete(){
        instance = null;

        normalWriter.interrupt();
        localWriter.interrupt();
        remoteWriter.interrupt();

        if (normalCount > 0) {
            System.out.println("Normal average time : " + normalTotal / normalCount + " ns (" + (float) normalTotal / (float) normalCount / 1000000f + " ms)");
        }
        if (localCount > 0) {
            System.out.println("Local average time : " + localTotal / localCount + " ns (" + (float) localTotal / (float) localCount / 1000000f + " ms)");
        }
        if (remoteCount > 0) {
            System.out.println("Remote average time : " + remoteTotal / remoteCount + " ns (" + remoteTotal / remoteCount / 1000000f + " ms)");
        }

        System.out.println("Total data sent : " + remoteSize / 1000 + " KB");
    }

    private String fileNameBuilder(String folder, String name, long timestamp){
        StringBuilder b = new StringBuilder();
        b.append(folder);
        b.append("\\");
        b.append(name);
        b.append("_");
        b.append(timestamp);
        b.append(".csv");
        return b.toString();
    }

    protected class Writer extends Thread {
        public Writer(String path) throws IOException {
            queue = new ConcurrentLinkedQueue<Result>();

            file = new FileWriter(path);
            file.write("Time,Size,Result" + System.lineSeparator());
        }

        private ConcurrentLinkedQueue<Result> queue = null;
        private Boolean run = true;
        private FileWriter file = null;

        @Override
        public void run(){
            while (run){
                write();
            }
        }

        public synchronized void put(Result result){
            queue.add(result);
            notify();
        }

        private synchronized void write(){
            while (queue.size() == 0) {
                try{
                    wait();
                }catch (InterruptedException e){
                    delete();
                    run = false;
                }
            }

            while (queue.size() > 0){
                Result item = queue.poll();
                try {
                    file.write(item.getTime() + "," + item.getSize() + "," + item.getResult() + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void delete(){
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Result{
        private Long time;
        private int size;
        private int result;

        public Result(){

        }

        public Result(Long time, int size, int result) {
            this.time = time;
            this.size = size;
            this.result = result;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }
}
