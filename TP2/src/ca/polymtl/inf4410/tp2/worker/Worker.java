package ca.polymtl.inf4410.tp2.worker;

import ca.polymtl.inf4410.tp2.operations.Operations;
import ca.polymtl.inf4410.tp2.shared.Operation;
import ca.polymtl.inf4410.tp2.shared.RequestRejectedException;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;

/**
 * @author Alexandre on 11/12/2015.
 */
public class Worker implements ServerInterface {

    private WorkerConfig config;
    
    private Random rand = new Random();
    private Random randMalice = new Random();

    public Worker(WorkerConfig config) {
        this.config = config;
        export();
    }

    private void export(){
        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, config.getPort());

            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind(config.getName(), stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lance ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public int executeOperations(List<Operation> operations) throws RemoteException, RequestRejectedException {
        if (accept(operations)) {
            int r = 0;
            for (Operation op : operations) {
                switch (op.name) {
                    case "fib":
                        System.out.println("Executing fib " + op.operand);
                        r += Operations.fib(op.operand);
                        break;
                   case "prime":
                        System.out.println("Executing prime " + op.operand);
                        r += Operations.prime(op.operand);
                       break;
                }

                r = r % 5000;
            }
            
            int malice = this.config.getMalice();
            if(malice > 0)
            {
            	int maliceNumber = randMalice.nextInt(100 + 1);
            	if(maliceNumber <= malice)
            	{
            		r += maliceNumber;
            		r = r % 5000;
            	}
            }
            
            System.out.println("Processed " + operations.size() + " operations");
            return r;
        } else {
            throw new RequestRejectedException();
        }
    }
    
    private Boolean accept(List<Operation> operations){
    	int capacity = config.getCapacity();
    	if(operations.size() <= capacity){
    		return true;
    	}
    	else{
    		int rejectRate = (operations.size()- capacity)/(9*capacity) *100;
    		int randomNumber = rand.nextInt(100 + 1);
    		if(randomNumber > rejectRate){
    			return true;
    		}
    		else{
    			return false;
    		}
    	}
    }
}
