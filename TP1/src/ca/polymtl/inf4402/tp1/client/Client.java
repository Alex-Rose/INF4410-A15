package ca.polymtl.inf4402.tp1.client;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import ca.polymtl.inf4402.tp1.shared.RemoteFile;
import ca.polymtl.inf4402.tp1.shared.ServerInterface;
import javafx.util.Pair;

/**
 * Classe de base pour l'application client
 * <p>
 *     Cette classe contient le main de l'application client.
 *     On y retrouve aussi les différents appels des fonctionnalités
 *     du logiciel
 * <p>
 *     Le code pour les mesures de performance (benchmark) se trouve aussi
 *     dans cette classe.
 */
public class Client {
	public static void main(String[] args) {
        Pair<String, String> arguments = parseArguments(args);

        String action = arguments.getKey();
        String value = arguments.getValue();

        if (action != null){

            if (action.equals("benchmark")){
                FakeClient client = new FakeClient(value);
            }
            else if (action.equals("setServer")){
                setServer(value);
            }
            else {
                try {
                    Client client = new Client();

                    if (value == null) {
                        Method method = client.getClass().getMethod(action);
                        method.invoke(client);
                    }
                    else {
                        Method method = client.getClass().getMethod(action, String.class);
                        method.invoke(client, value);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    printHelp();
                } catch (InvocationTargetException e) {
                    System.err.println(e.getTargetException().getMessage());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            printHelp();
        }

	}

    public static Pair<String, String> parseArguments(String[] args){
        String action = null;
        if (args.length > 0) {
            action = args[0];
        }

        String param = null;
        if (args.length > 1) {
            param = args[1];
        }

        return new Pair<String, String>(action, param);
    }

    public static void printHelp(){
        System.out.println("Client help file \n\n");
        System.out.println("Usage : client action [parameter]\n");
        System.out.println("Actions : ");
        System.out.println("    create         : Creates an empty file on the server ");
        System.out.println("    list           : Print file list from the server");
        System.out.println("    syncLocalDir   : Download all files from server");
        System.out.println("                     WARNING Overwrites local content");
        System.out.println("    get            : Downloads file from the server");
        System.out.println("                     WARNING Overwrites local content");
        System.out.println("    lock           : Lock file for editing");
        System.out.println("    push           : Send new file version to server");
        System.out.println("    setServer      : Sets the server configuration (see options)\n");
        System.out.println("Server options : ");
        System.out.println("    local          : Use a server on the localmachine ");
        System.out.println("    [hostname]     : Use the URI to resolve remote host");
    }

    FakeServer localServer = null; // Pour tester la latence d'un appel de fonction normal
	private ServerInterface serverStub = null;
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;
    private Boolean verbose = true;
    private Boolean isBenchmark = false;
    private int uid = -1; // user ID


    /**
     * Constructeur de Client
     */
    public Client() {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		serverStub = loadServerStub();

        setClientID();
	}

    private void setClientID(){
        try {
            int uid = -1;
            File file = new File(".uid");

            if (!file.exists()) {
                uid = serverStub.generateClientId();
                FileWriter writer = new FileWriter(file);
                writer.write(Integer.toString(uid));
                writer.close();
            } else {
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String ln = br.readLine();
                uid = Integer.parseInt(ln);
                br.close();
            }

            this.uid = uid;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println(".uid file corrupted. Please run again.");
            int uid = 0;
            try {
                uid = serverStub.generateClientId();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            try {
                FileWriter writer = new FileWriter(".uid");
                writer.write(Integer.toString(uid));
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void setServer(String value){
        try {
            FileWriter writer = new FileWriter(".server");
            writer.write(value);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private ServerInterface loadServerStub() {
		ServerInterface stub = null;
        String hostname = null;
        try {
            FileReader reader = new FileReader(".server");
            BufferedReader buffer = new BufferedReader(reader);
            String value = buffer.readLine();
            buffer.close();

            if (value.equals("local")){
                hostname = "127.0.0.1";
            }
            else {
                hostname = value;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Server configuration not found. Please run setServer");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

    /**
     * Create an empty file on the server
     * @param name file name
     * @throws RemoteException
     */
    public void create(String name) throws RemoteException{
        serverStub.create(name);
    }

    /**
     * Print remote file list to system output
     * @throws RemoteException
     */
    public void list() throws RemoteException{
        LinkedList<String> list = serverStub.list();
        for (Iterator<String> i = list.iterator(); i.hasNext();){
            System.out.println(i.next());
        }
    }

    public void syncLocalDir() throws RemoteException{

    }

    public void get(String name) throws RemoteException{

    }

    public void lock(String name) throws RemoteException{

    }

    public void push(String name) throws RemoteException{

    }

    /**
     * Run various tests. Can be used to trigger automated testing.
     * This method will simulate a few actions
     * @param filename File to be used in tests
     */
    public void test(String filename){
        try {
            int uid = -1;
            File file = new File(".uid");

            if (!file.exists()){
                uid = localServerStub.generateClientId();
                FileWriter writer = new FileWriter(file);
                writer.write(Integer.toString(uid));
                writer.close();
            }
            else {
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String ln = br.readLine();
                uid = Integer.parseInt(ln);
                br.close();
            }

            System.out.println("Client Id : " + uid);


            byte[] data = FileHelper.readFile(filename);
            byte[] checksum = FileHelper.getChecksum(data);

            serverStub.create(filename);
            serverStub.lock(filename, uid, new byte[]{-1});
            serverStub.push(filename, data, uid);
            RemoteFile rf = serverStub.get(filename, new byte[]{-1});
            FileHelper.writeFile("output.txt", rf.getContent());

            byte[] data2 = FileHelper.readFile("output.txt");
            byte[] checksum2 = FileHelper.getChecksum(data2);

            boolean eq = Arrays.equals(data, data2);
            boolean eq2 = Arrays.equals(checksum, checksum2);
            boolean eq3 = Arrays.equals(checksum, checksum2);


        } catch (RemoteException e) {
            System.err.println(e.getCause().getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println(".uid file corrupted. Please run again.");
            int uid = 0;
            try {
                uid = serverStub.generateClientId();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            try {
                FileWriter writer = new FileWriter(".uid");
                writer.write(Integer.toString(uid));
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
