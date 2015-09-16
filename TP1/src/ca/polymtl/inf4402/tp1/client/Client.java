package ca.polymtl.inf4402.tp1.client;

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

        Boolean benchmark = false;

        if (args.length > 1) {
            benchmark = args[1].toLowerCase().equals("benchmark");
        }

		Client client = new Client(distantHostname);
        client.benchmark = benchmark;

        if (benchmark) {
            client.benchmark(100, 1000);
        }

        Benchmark.getInstance().flushAndDelete();
	}

	FakeServer localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;
    private Boolean verbose = true;
    public Boolean benchmark = false;

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

        if (benchmark) {
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
            if (benchmark) {
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

            if (benchmark) {
                Benchmark.getInstance().writeRemoteAsync(end - start, data.length, 0);
            }
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
}
