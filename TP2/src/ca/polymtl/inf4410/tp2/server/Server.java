package ca.polymtl.inf4410.tp2.server;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import ca.polymtl.inf4410.tp2.shared.Operation;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

public class Server implements ServerInterface{
	
	private int capacity;
	private int malice;
	
	public static void main(String[] args) {
		if(args.length <= 1){
			System.out.println("Invalid arguments");
			return;
		}
		
		int capacity = Integer.parseInt(args[0]);
		int malice = Integer.parseInt(args[1]);
		if(malice < 0 || malice > 100){
			System.out.println("Invalid malice value (must be between 0 and 100");
		}
		Server server = new Server(capacity, malice);
		server.run();
	}
	
	public Server(int capacityParam, int maliceParam){
		capacity = capacityParam;
		malice = maliceParam;
	}
	
	@Override
	public int executeOperations(List<Operation> operations) {
		
		
		return 0;
	}
	
	private void run(){
		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 14001);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	


}
