package ca.polymtl.inf4410.tp2.worker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexandre on 11/12/2015.
 */
public class Main {
    public static Worker instance;
    public static String name;
    public static int port;
    public static int capacity;
    public static int malice;
    public static String configFile;

    public static void main(String[] args){

        try {
            parseArguments(args);
        } catch (Exception e) {
            e.printStackTrace();
            printHelp();
            return;
        }

        try {
            WorkerConfig config = null;
            if (configFile != null) {
                config = WorkerConfig.readFromFile(configFile);
            } else {
                config = new WorkerConfig();
                config.setName(name);
                config.setConfigFile(configFile);
                config.setPort(port);
                config.setCapacity(capacity);
                config.setMalice(malice);
            }

            instance = new Worker(config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseArguments(String[] args) throws Exception {
        configFile = null;
        name = null;
        port = -1;
        capacity = -1;
        malice = -1;


        for (int i = 0; i < args.length; i++) {
            // Name file
            if (args[i].equals("-n") || args[i].equals("--name")) {
                if (++i < args.length) {
                    name = args[i];
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // Config file
            else if (args[i].equals("-c") || args[i].equals("--config")) {
                if (++i < args.length) {
                    configFile = args[i];
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // port
            else if (args[i].equals("-p") || args[i].equals("--port")) {
                if (++i < args.length) {
                    port = Integer.parseInt(args[i]);
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // capacity
            else if (args[i].equals("-n") || args[i].equals("--capacity")) {
                if (++i < args.length) {
                    capacity = Integer.parseInt(args[i]);
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // malice
            else if (args[i].equals("-m") || args[i].equals("--malice")) {
                if (++i < args.length) {
                    malice = Integer.parseInt(args[i]);
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
        }

        if (configFile == null) {
            if (name == null) {
                throw new Exception("Need a name");
            }

            if (port == -1) {
                throw new Exception("Need a port");
            }

            if (capacity == -1) {
                throw new Exception("Need a capacity");
            }

            if (malice == -1) {
                throw new Exception("Need a malice index");
            }
        }
    }

    /**
     * Print usage to System out
     */
    public static void printHelp(){
        System.out.println("Worker help file \n\n");
        System.out.println("Usage : worker [parameters]\n");
        System.out.println("parameters : ");
        System.out.println("    -n, --name     : Name of RMI object to export");
        System.out.println("    -p, --port     : Port for RMI export");
        System.out.println("    -n, --capacity : Maximum capacity for this worker");
        System.out.println("    -m, --malice   : Malice index for this worker");
        System.out.println("    -c, --config   : Read configuration from xml file instead");
    }
}
