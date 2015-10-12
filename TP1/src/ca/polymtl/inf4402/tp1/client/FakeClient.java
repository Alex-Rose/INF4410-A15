package ca.polymtl.inf4402.tp1.client;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

import java.io.*;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

/**
 * Classe permettant de rouler les benchmark
 * <p>
 *     Cette classe contient le code pour se connecter au serveur local et distant
 *     puis pour mesurer la latence des appels.
 * <p>
 *     L'aggrégation des données se fait dans la classe {@link ca.polymtl.inf4402.tp1.client.Benchmark Benchmark}
 * <p>
 *     Created by Alexandre on 9/21/2015.
 */
public class FakeClient {
    /**
     * Constructeur de FakeClient
     * @param distantHostname Remote server hostname (IP ou nom de domaine)
     */
    public FakeClient(String distantHostname){
        localServer = new FakeServer();
        localServerStub = loadServerStub("127.0.0.1");
        distantServerStub = loadServerStub(distantHostname);

        initBenchmark();
        Benchmark.getInstance().flushAndDelete();
    }

    FakeServer localServer = null; // Pour tester la latence d'un appel de fonction normal
    private ServerInterface localServerStub = null;
    private ServerInterface distantServerStub = null;
    private boolean verbose = false;

    /**
     * Crée ou ouvre la connexion au serveur RMI, puis obtient un objet
     * d'interface {@link ca.polymtl.inf4402.tp1.shared.ServerInterface ServerInterface}
     * @param hostname server hostname (IP ou nom de domaine)
     * @return
     */
    private ServerInterface loadServerStub(String hostname) {
        ServerInterface stub = null;

        if (hostname == null){
            hostname = "127.0.0.1";
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
     * Crée les objets Benchmark et initie la boucle pour les différents benchmark
     */
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

    /**
     * Prend les résultats reçus en entrée et les écrit dans le dossier
     * results en format .csv
     * @param results Array de résultats
     */
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

    /**
     * Execute une série d'appels normaux, locaux et distants
     * @param nb Nombre d'appels à faire
     * @param size Taille de l'array passé en argument (10^n)
     */
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

    /**
     * Génère un array de byte aléatoire
     * @param size Taille de l'array
     * @return Array de byte aléatoire
     */
    private byte[] getRandomData(int size){
        byte[] data = new byte[size];
        Random rand = new Random();
        rand.nextBytes(data);

        return data;
    }

    /**
     * Method used by benchmark
     * @param data Data sent to server
     */
    private void appelNormal(byte[] data) {
        long start = System.nanoTime();
        localServer.execute(data);
        long end = System.nanoTime();

        if (verbose) {
            System.out.println("Temps écoulé appel normal: " + (end - start)
                    + " ns");
            System.out.println("Résultat appel normal: " + 0);
        }

        Benchmark.getInstance().writeNormalAsync(end - start, data.length, 0);

    }

    /**
     * Method used by benchmark
     * @param data Data sent to server
     */
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

            Benchmark.getInstance().writeLocalAsync(end - start, data.length, 0);

        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Method used by benchmark
     * @param data Data sent to server
     */
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

            Benchmark.getInstance().writeRemoteAsync(end - start, data.length, 0);

        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }
}
