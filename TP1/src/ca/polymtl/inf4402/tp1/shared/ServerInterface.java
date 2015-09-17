package ca.polymtl.inf4402.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	void execute(byte[] arg) throws RemoteException;
	int generateClientId() throws RemoteException;
    boolean create(String name) throws RemoteException;
    LinkedList<String> list() throws RemoteException;
    void syncLocalDir() throws RemoteException;
    RemoteFile get(String name, byte[] checksum) throws RemoteException;
    RemoteFile lock(String name, int clientId, byte[] checksum) throws RemoteException;
    boolean push(String name, byte[] data, int clientId) throws RemoteException;
}
