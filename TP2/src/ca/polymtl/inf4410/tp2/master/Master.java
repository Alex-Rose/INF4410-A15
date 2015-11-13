package ca.polymtl.inf4410.tp2.master;

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

public class Master {
	private ArrayList<ServerInterface> serverStubs;
	private ArrayList<Operation> operations;
    private MasterConfig config;

    public Master(MasterConfig config) throws IOException {
        this.config = config;
        populateOperations();
        initializeServerStubs();

        try {
			System.out.println("Result : " + serverStubs.get(0).executeOperations(null));
		} catch (RequestRejectedException e) {
			System.out.println(e.getMessage());
		}
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
	        } catch (NotBoundException e) {
	            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
	        } catch (AccessException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        } catch (RemoteException e) {
	            System.out.println("Erreur: " + e.getMessage());
	        }

            serverStubs.add(stub);
		}
	}
	

	private static void executeOperations(){
		
		
		
		
	}
}
