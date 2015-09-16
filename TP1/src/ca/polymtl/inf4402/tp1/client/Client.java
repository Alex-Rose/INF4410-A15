package ca.polymtl.inf4402.tp1.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
		}

        Boolean isBenchmark = false;

        if (args.length > 1) {
            isBenchmark = args[1].toLowerCase().equals("benchmark");
        }

		Client client = new Client(distantHostname);
        client.isBenchmark = isBenchmark;

        if (isBenchmark) {
            client.initBenchmark();
        }

        Benchmark.getInstance().flushAndDelete();
	}

    FakeServer localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;
    private Boolean verbose = true;
    private Boolean isBenchmark = false;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
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

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

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
}
