package ca.polymtl.inf4402.tp1.client;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ca.polymtl.inf4402.tp1.shared.RemoteFile;
import ca.polymtl.inf4402.tp1.shared.ServerInterface;

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
        String[] arguments = parseArguments(args);

        String action = arguments[0];
        String value = arguments[1];

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
                    e.printStackTrace();
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

    /**
     * Parse CLI arguments.
     * @param args argument array
     * @return action, option
     */
    public static String[] parseArguments(String[] args){
        String action = null;
        if (args.length > 0) {
            action = args[0];
        }

        String param = null;
        if (args.length > 1) {
            param = args[1];
        }

        return new String[]{action, param};
    }

    /**
     * Print usage to System out
     */
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

    /**
     * Get client ID from cached file. If file is not found, get new client ID
     * from server
     */
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

    /**
     * Cache the connection settings
     * @param value hostname
     */
    public static void setServer(String value){
        try {
            FileWriter writer = new FileWriter(".server");
            writer.write(value);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to RMI server and get server object handle of type ServerInterface
     * Server hostname defined by {@link ca.polymtl.inf4402.tp1.client.Client setServer}
     * @return ServerInterface
     */
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

    /**
     * Download all file from server.
     * @throws RemoteException
     */
    public void syncLocalDir() throws RemoteException{
        LinkedList<String> list = serverStub.list();
        for (Iterator<String> i = list.iterator(); i.hasNext();){
            String name = i.next();
            System.out.print(name);

            byte[] checksum = FileHelper.getChecksum(name);
            RemoteFile file = serverStub.get(name, checksum);
            if (file != null){
                FileHelper.writeFile(name, file.getContent());
                System.out.println(" [Downloaded]");
            }
            else {
                System.out.println(" [Cached]");
            }
        }
    }

    /**
     * Download file from server, unless the local version
     * is up to date.
     * @param name Path of the find to download from server
     * @throws RemoteException
     */
    public void get(String name) throws RemoteException{
        byte[] checksum = FileHelper.getChecksum(name);
        RemoteFile file = serverStub.get(name, checksum);
        if (file != null){
            FileHelper.writeFile(name, file.getContent());
        }
        else {
            System.out.println("Local file already up-to-date");
        }
    }

    /**
     * Try to lock file
     * @param name Path of file to lock
     * @throws RemoteException if file is already locked or local file out of sync
     */
    public void lock(String name) throws RemoteException{
        byte[] checksum = FileHelper.getChecksum(name);
        serverStub.lock(name, uid, checksum);
    }

    /**
     * Upload file to server
     * @param name file to upload
     * @throws RemoteException
     */
    public void push(String name) throws RemoteException{
        byte[] data = FileHelper.readFile(name);
        if (data != null){
            serverStub.push(name, data, uid);
        }
    }

    /**
     * Run various tests. Can be used to trigger automated testing.
     * This method will simulate a few actions
     * @param filename File to be used in tests
     */
    public void test(String filename) {
        Random r = new Random();
        List<String> words = new ArrayList<String>(){{
            add("Bonjour");
            add("Monde");
            add("Hello");
            add("World");
            add("!");
        }};
        for (int i = 0; i < 1000; i++) {
            try {
                switch (r.nextInt(12)){
                    case 0:
                        create(filename);
                        break;
                    case 1:
                    case 2:
                    case 3:
                        get(filename);
                        break;
                    case 4:
                    case 5:
                    case 6:
                        lock(filename);
                        break;
                    case 7:
                    case 8:
                    case 9:
                        push(filename);
                        break;
                    case 10:
                        System.out.println("Appending word");
                        try {
                            List<String> text = Files.readAllLines(Paths.get(filename));
                            text.add(words.get(r.nextInt(words.size())));
                            String finalText = "";
                            for (Iterator<String> it = text.iterator(); it.hasNext();){
                                finalText += it.next() + " ";
                            }

                            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
                            writer.write(finalText);
                            writer.close();

                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }

                        break;
                    case 11:
                        System.out.println("Deleting file");
                        try{
                            File file = new File(filename);
                            if (file.exists()){
                                file.delete();
                            }
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                        break;
                    default:
                        System.out.println("no op.");
                        break;
                }
            } catch (RemoteException e) {
                System.err.println(e.getCause().getMessage());
            }
        }

        try {
            // If file is locked after last instruction
            push(filename);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
