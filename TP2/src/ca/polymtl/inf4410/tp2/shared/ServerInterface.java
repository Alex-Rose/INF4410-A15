package ca.polymtl.inf4410.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote{
    int executeOperations(List<Operation> operations) throws RemoteException, RequestRejectedException;

    /**
     * USE ONLY FOR BENCHMARKS
     * @return malice
     * @throws RemoteException
     */
    int getMaliceValue() throws RemoteException;

    /**
     * USE ONLY FOR BENCHMARKS
     * @return capacity
     * @throws RemoteException
     */
    int getCapacity() throws RemoteException;
}
