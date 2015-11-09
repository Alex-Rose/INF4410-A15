package ca.polymtl.inf4410.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.nio.file.*;

import ca.polymtl.inf4410.tp2.shared.*;

public class Client {
	private static ServerInterface[] serverStubs;
	private static Boolean secureMode;
	private static List<Operation> operations;
	
	public static void main(String[] args){
		String[] arguments;
		try{
			arguments = parseArguments(args);
		}
		catch(Exception ex){
			System.out.println("Invalid arguments");
			return;
		}
		
		try {
			populateOperations(arguments[0]);
		} catch (IOException e) {
			System.out.println("An error occurred while reading the operation file");
		}
		setMode(arguments[1]);
		
		String[] serverHosts = (String[]) Arrays.copyOfRange(arguments, 2, 5);
		initializeServerStubs(serverHosts);
		
		executeOperations();
	}
	
	private static void populateOperations(String fileLocation) throws IOException{
		operations = new ArrayList<Operation>();
		for (String line : Files.readAllLines(Paths.get(fileLocation))) {
			String[] splitLine = line.split("\\s+");
			Operation operation = new Operation(splitLine[0], Integer.parseInt(splitLine[1]));
			operations.add(operation);
		}
	}
	
	private static void setMode(String mode){
		secureMode = (mode == "secure");
	}
	
	private static void initializeServerStubs(String[] serverHosts){
		serverStubs = new ServerInterface[3];
		for (int i = 0; i < serverHosts.length; i++){
			ServerInterface stub = null;
			try {
	            Registry registry = LocateRegistry.getRegistry(serverHosts[i]);
	            stub = (ServerInterface) registry.lookup("server");
	        } catch (NotBoundException e) {
	            System.out.println("Erreur: Le nom '" + e.getMessage()
	                    + "' n'est pas défini dans le registre.");
	        } catch (AccessException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        } catch (RemoteException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        }
			serverStubs[i] = stub;
		}
	}
	
	
	private static String[] parseArguments(String[] args){
        String operationFile = null;
        if (args.length > 0) {
            operationFile = args[0];
        }
        
        String mode = null;
        if (args.length > 1) {
            mode = args[1];
        }

        String[] servers = new String[3];
        if (args.length > 4) {
	        for (int i = 2; i < args.length; i++) {
	            servers[i-2] = args[i]; 
	        }
        }

        return new String[]{operationFile, mode, servers[0], servers[1], servers[2]};
    }
	
	private static void executeOperations(){
		
		int serverLoad = operations.size()/3;
		
		
	}
}
