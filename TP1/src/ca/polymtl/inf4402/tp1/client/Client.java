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
 * </p>
 * <p>
 *     Le code pour les mesures de performance (benchmark) se trouve aussi
 *     dans cette classe.
 * </p>
 */
public class Client {
	public static void main(String[] args) {
        Pair<String, String> arguments = parseArguments(args);

        String action = arguments.getKey();
        String value = arguments.getValue();

        if (action != null){

            if (action.equals("benchmark")){
                Client client = new Client();
                client.isBenchmark = true;
                client.initBenchmark();
                Benchmark.getInstance().flushAndDelete();
            }
            else if (action.equals("setServer")){
                setServer(value);
            }
            else {
                try {
                    Client client = new Client();
                    Method method = client.getClass().getMethod(action, String.class);
                    method.invoke(client, value);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    printHelp();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
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
	}

    private void setClientID(){
        try {
            int uid = -1;
            File file = new File(".uid");

            if (!file.exists()) {
                uid = localServerStub.generateClientId();
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

    private void initBenchmark() {
        this.verbose = false;

        Benchmark.Result[][] results = new Benchmark.Result[3][8];

        for (int i = 1; i <= 8; i++) {
            int hardcap = 100000000;
            int size = (int)Math.pow(10, i);
            int cnt = 100;
            if (size * cnt > hardcap){
                cnt /= 10;
            }

            if (size * cnt > hardcap){
                cnt /= 10;
            }

            System.out.println(System.lineSeparator() + cnt + " iterations at 10^" + i + " bytes");
            benchmark(cnt, size);

            Benchmark benchmark = Benchmark.getInstance();
            results[0][i-1] = benchmark.getResult("normal");
            results[1][i-1] = benchmark.getResult("local");
            results[2][i-1] = benchmark.getResult("remote");
            benchmark.flushAndDelete();
        }

        saveResults(results);
    }

    private void saveResults(Benchmark.Result[][] results){
        try {
            String resultsDir = "results";
            File folder = new File(resultsDir);

            if (!folder.exists()){
                folder.mkdir();
            }

            long timestamp = System.currentTimeMillis() / 1000L;

            FileWriter file = new FileWriter("results/averages_" + timestamp + ".csv");
            file.write("Size,Normal,Local,Remote" + System.lineSeparator());

            for (int i = 0; i < results[0].length; i++){
                file.write(results[0][i].getSize() + ","
                        + results[0][i].getTime() + ","
                        + results[1][i].getTime() + ","
                        + results[2][i].getTime() +
                        System.lineSeparator());
            }

            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void benchmark(int nb, int size){
        byte[] data = getRandomData(size);

        for (int i = 0; i < nb; i++){
            appelNormal(data);
        }

        if (localServerStub != null) {
            for (int i = 0; i < nb; i++) {
                appelRMILocal(data);
            }
        }

        if (distantServerStub != null) {
            for (int i = 0; i < nb; i++) {
                appelRMIDistant(data);
            }
        }
    }

    private byte[] getRandomData(int size){
        byte[] data = new byte[size];
        Random rand = new Random();
        rand.nextBytes(data);

        return data;
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

	private void appelNormal(byte[] data) {
		long start = System.nanoTime();
		localServer.execute(data);
		long end = System.nanoTime();

        if (verbose) {
            System.out.println("Temps écoulé appel normal: " + (end - start)
                    + " ns");
            System.out.println("Résultat appel normal: " + 0);
        }

        if (isBenchmark) {
            Benchmark.getInstance().writeNormalAsync(end - start, data.length, 0);
        }
	}

	private void appelRMILocal(byte[] data) {
		try {
			long start = System.nanoTime();
			localServerStub.execute(data);
			long end = System.nanoTime();

            if (verbose) {
                System.out.println("Temps écoulé appel RMI local: " + (end - start)
                        + " ns");
                System.out.println("Résultat appel RMI local: " + 0);
            }
            if (isBenchmark) {
                Benchmark.getInstance().writeLocalAsync(end - start, data.length, 0);
            }
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void appelRMIDistant(byte[] data) {
		try {
			long start = System.nanoTime();
			distantServerStub.execute(data);
			long end = System.nanoTime();

            if (verbose) {
                System.out.println("Temps écoulé appel RMI distant: "
                        + (end - start) + " ns");
                System.out.println("Résultat appel RMI distant: " + 0);
            }

            if (isBenchmark) {
                Benchmark.getInstance().writeRemoteAsync(end - start, data.length, 0);
            }
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

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
