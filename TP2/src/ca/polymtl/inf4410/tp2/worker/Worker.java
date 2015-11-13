package ca.polymtl.inf4410.tp2.worker;

import ca.polymtl.inf4410.tp2.operations.Operations;
import ca.polymtl.inf4410.tp2.shared.Operation;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * @author Alexandre on 11/12/2015.
 */
public class Worker implements ServerInterface {

    private WorkerConfig config;

    public Worker(WorkerConfig config) {
        this.config = config;
        export();
    }

    private void export(){
        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, config.getPort());

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(config.getName(), stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lanc� ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public int executeOperations(List<Operation> operations) throws RemoteException{
        int r = 0;
        for (Operation op : operations) {
            r += Operations.fib(op.operand);
            r = r % 5000;
        }

        System.out.println("Processed " + operations.size() + " operations");
        return r;
    }
}
