package ca.polymtl.inf4410.tp2.master;

import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IntSummaryStatistics;

/**
 * @author Alexandre on 11/12/2015.
 */
public class Main {
    public static Master instance;
    public static String operationFile;
    public static boolean safeMode;
    public static ArrayList<Pair<String, String>> servers;
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
                System.err.println("Un fichier de configuration est necessaire");
                return;
            }
            if(config.isSafeMode())
            	instance = new MasterSafe(config);
            else
            	instance = new MasterUnsafe(config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseArguments(String[] args) throws Exception {
        safeMode = false;
        servers = new ArrayList<>();
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
            // Safe mode
            else if (args[i].equals("-f") || args[i].equals("--safe")) {
                safeMode = true;
            }

            // Config file
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
