package ca.polymtl.inf4410.tp2.master;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexandre on 11/12/2015.
 */
public class Main {
    public static Master instance;
    public static String operationFile;
    public static boolean safeMode;
    public static ArrayList<String> servers;
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
            MasterConfig config = null;
            if (configFile != null) {
                config = MasterConfig.readFromFile(configFile);
            } else {
                config = new MasterConfig();
            }

            instance = new Master(config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseArguments(String[] args) throws Exception {
        safeMode = false;
        servers = new ArrayList<String>();
        operationFile = null;
        configFile = null;

        for (int i = 0; i < args.length; i++) {
            // Operation file
            if (args[i].equals("-f") || args[i].equals("--file")) {
                if (++i < args.length) {
                    operationFile = args[i];
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // Servers general
            else if (args[i].equals("-s") || args[i].equals("--servers")) {
                if (++i < args.length) {
                    for (String s : args[i].split(",")) {
                        servers.add(s);
                    }
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // Servers labo l4712
            else if (args[i].equals("-l") || args[i].equals("--labo")) {
                if (++i < args.length) {
                    for (String s : args[i].split(",")) {
                        int nb = Integer.parseInt(s);
                        servers.add(String.format("l4712-%02d.info.polymtl.ca", nb));
                    }
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
            // Servers labo l4712
            else if (args[i].equals("-f") || args[i].equals("--safe")) {
                safeMode = true;
            }

            // Servers labo l4712
            else if (args[i].equals("-c") || args[i].equals("--config")) {
                if (++i < args.length) {
                    configFile = args[i];
                } else {
                    throw new Exception("Invalid arguments");
                }
            }
        }

        if (configFile == null) {
            if (servers.size() == 0) {
                throw new Exception("Need at least one server");
            }

            if (operationFile == null) {
                throw new Exception("Need operation file");
            } else {
                File file = new File(operationFile);
                if (!file.exists()) {
                    throw new Exception("Operation file not found");
                } else if (file.isDirectory()) {
                    throw new Exception("Invalid operation file (is directory)");
                }
            }
        }
    }

    /**
     * Print usage to System out
     */
    public static void printHelp(){
        System.out.println("Master help file \n\n");
        System.out.println("Usage : master [parameters]\n");
        System.out.println("parameters : ");
        System.out.println("    -f, --file     : Operation file to use ");
        System.out.println("    -s, --servers  : Calculation units to use (full name), separated by comas");
        System.out.println("    -l, --labo     : Calculation units number to use (machine in l4712), separated by comas");
        System.out.println("                     separated by comas");
        System.out.println("    -f, --safe     : Start in safe mode");
        System.out.println("    -c, --config   : Read configuration from xml file instead");
    }
}
