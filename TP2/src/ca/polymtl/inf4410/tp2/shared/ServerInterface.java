package ca.polymtl.inf4410.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote{
	int executeOperations(List<Operation> operations) throws RemoteException;
}
