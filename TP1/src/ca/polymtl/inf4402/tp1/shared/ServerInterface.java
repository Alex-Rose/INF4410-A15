package ca.polymtl.inf4402.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public interface ServerInterface extends Remote {

    /**
     * Mock method for benchmark
     * @param a
     * @param b
     * @return
     * @throws RemoteException
     */
    int execute(int a, int b) throws RemoteException;

    /**
     * Mock method for benchmark
     * @param arg
     * @throws RemoteException
     */
	void execute(byte[] arg) throws RemoteException;

    /**
     * Generate a new random client ID
     * @return random client ID
     * @throws RemoteException
     */
	int generateClientId() throws RemoteException;

    /**
     * Create new empty file
     * @param name file name
     * @return true if succeeded
     * @throws RemoteException if file already exists
     */
    boolean create(String name) throws RemoteException;

    /**
     * Get the file list. Names and attributes only, content is not downloaded
     * @return file list
     * @throws RemoteException
     */
    LinkedList<String> list() throws RemoteException;

    HashMap<String, RemoteFile> syncLocalDir() throws RemoteException;

    /**
     * Download the file from server if different
     * @param name Name of the file to get
     * @param checksum Checksum of the local file
     * @return RemoteFile if found and different, null otherwise
     * @throws RemoteException if file not found
     */
    RemoteFile get(String name, byte[] checksum) throws RemoteException;

    /**
     * Lock file on the server if not already locked an file is in sync
     * @param name file to lock
     * @param clientId user ID
     * @param checksum local checksum
     * @return locked file
     * @throws RemoteException if file not found or file checksum differs
     */
    RemoteFile lock(String name, int clientId, byte[] checksum) throws RemoteException;

    /**
     * Upload file to server
     * @param name file to be uploaded
     * @param data file content
     * @param clientId user ID
     * @return true if succeeded
     * @throws RemoteException file locked by another user, file not locked, file not found
     */
    boolean push(String name, byte[] data, int clientId) throws RemoteException;
}
