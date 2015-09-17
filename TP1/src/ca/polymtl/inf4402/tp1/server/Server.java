package ca.polymtl.inf4402.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import ca.polymtl.inf4402.tp1.shared.RemoteFile;
import ca.polymtl.inf4402.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
        super();

        files = new HashMap<String, RemoteFile>();
        fileList = new LinkedList<String>();
	}

    protected HashMap<String, RemoteFile> files;
    protected HashMap<String, Integer> lockTable;
    protected LinkedList<String> fileList;
    private boolean verbose = true;

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 14001);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * Méthode accessible par RMI. Additionne les deux nombres passés en
	 * paramètre.
	 */
	@Override
	public int execute(int a, int b) throws RemoteException {
		return a + b;
	}

	/*
	 * Autre méthode accessible par RMI. Elle prend un tableau afin de pouvoir
	 * lui envoyer des arguments de taille variable.
	 */
	@Override
	public void execute(byte[] arg) throws RemoteException {
		return;
	}

	@Override
	public int generateClientId() throws RemoteException {
        Random r = new Random();
        int id = Math.abs(r.nextInt());
        output("New client ID created : " + id);
        return id;
	}

    @Override
	public boolean create(String name) throws RemoteException {
		boolean result = false;

        if (!files.containsKey(name)) {
            files.put(name, new ServerFile(name));
            fileList.add(name);
            result = true;
            output("Created new file : " + name);
        }
        else {
            throw new RemoteException("File already exsits");
        }

        return result;
	}

	@Override
	public LinkedList<String> list() throws RemoteException {
		return fileList;
	}

	@Override
	public void syncLocalDir() throws RemoteException {

	}

	@Override
	public RemoteFile get(String name, byte[] checksum) throws RemoteException {
		RemoteFile file = getFileIfExists(name);
        if (file != null){
            if (Arrays.equals(file.getChecksum(), checksum)){
                file = null;
            }
        }
        else
        {
            throw new RemoteException("File not found!");
        }

        return file;
	}

	@Override
	public RemoteFile lock(String name, int clientId, byte[] checksum) throws RemoteException {
		RemoteFile file = get(name, checksum);
        if (file != null) {
            if (!lockTable.containsKey(name)) {
                lockTable.put(name, clientId);
            }
            else{
                throw new RemoteException("File already locked.");
            }
        }

        return file;
    }

	@Override
	public boolean push(String name, byte[] data, int clientId) throws RemoteException {
        RemoteFile file = getFileIfExists(name);

        if (file != null) {
            if (lockTable.containsKey(name)) {
                if (lockTable.get(name) == clientId) {
                    fileList.add(file.getName());
                    files.put(file.getName(), file);
                }
                else{
                    throw new RemoteException("File locked by another user.");
                }
            }
            else{
                throw new RemoteException("File must be locked.");
            }
        }
        else{
            throw new RemoteException("File not found!");
        }

        return true;
	}

    protected RemoteFile getFileIfExists(String name){
        RemoteFile file = null;
        if (files.containsKey(name)){
            file = files.get(name);
        }

        return file;
    }

    private void output(String msg) {
        if (verbose) {
            System.out.println(msg);
        }
    }
}
