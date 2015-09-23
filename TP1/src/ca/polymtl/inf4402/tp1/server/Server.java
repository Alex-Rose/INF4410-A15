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
        lockTable = new HashMap<String, Integer>();
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

    /**
     * Generate a new random client ID
     * @return random client ID
     * @throws RemoteException
     */
	@Override
	public int generateClientId() throws RemoteException {
        Random r = new Random();
        int id = Math.abs(r.nextInt());
        output("New client ID created : " + id);
        return id;
	}

    /**
     * Create new empty file
     * @param name file name
     * @return true if succeeded
     * @throws RemoteException if file already exists
     */
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

    /**
     * Get the file list. Names and attributes only, content is not downloaded
     * @return file list
     * @throws RemoteException
     */
	@Override
	public LinkedList<String> list() throws RemoteException {
		return fileList;
	}

	@Override
	public HashMap<String, RemoteFile> syncLocalDir() throws RemoteException {
        return files;
	}

    /**
     * Download the file from server if different
     * @param name Name of the file to get
     * @param checksum Checksum of the local file
     * @return RemoteFile if found and different, null otherwise
     * @throws RemoteException if file not found
     */
	@Override
	public RemoteFile get(String name, byte[] checksum) throws RemoteException {
		RemoteFile file = getFileIfExists(name);
        if (file != null){
            if (Arrays.equals(file.getChecksum(), checksum)){
                file = null;
            }
            else {
                output("Uploaded file to client : " + name);
            }
        }
        else
        {
            throw new RemoteException("File not found!");
        }

        return file;
	}

    /**
     * Lock file on the server if not already locked an file is in sync
     * @param name file to lock
     * @param clientId user ID
     * @param checksum local checksum
     * @return locked file
     * @throws RemoteException if file not found or file checksum differs
     */
	@Override
	public RemoteFile lock(String name, int clientId, byte[] checksum) throws RemoteException {
		RemoteFile file = get(name, checksum);
        if (file != null) {
            if (!lockTable.containsKey(name)) {
                lockTable.put(name, clientId);
                output("Locked file : " + name);
            }
            else{
                throw new RemoteException("File already locked.");
            }
        }
        else
        {
            throw new RemoteException("Client file differs from server.");
        }

        return file;
    }

    /**
     * Upload file to server
     * @param name file to be uploaded
     * @param data file content
     * @param clientId user ID
     * @return true if succeeded
     * @throws RemoteException file locked by another user, file not locked, file not found
     */
	@Override
	public boolean push(String name, byte[] data, int clientId) throws RemoteException {
        RemoteFile remoteFile = getFileIfExists(name);

        if (remoteFile != null) {
            if (lockTable.containsKey(name)) {
                if (lockTable.get(name) == clientId) {
                    ServerFile file = (ServerFile)remoteFile;
                    file.setContent(data);

                    if (!fileList.contains(file.getName())) {
                        fileList.add(file.getName());
                    }

                    if (files.containsKey(file.getName())){
                        files.remove(file.getName());
                    }

                    files.put(file.getName(), file);

                    lockTable.remove(name);

                    output("File pushed : " + name);
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
