package ca.polymtl.inf4402.tp1.shared;

/**
 * Created by Alexandre on 9/16/2015.
 */
public interface RemoteFile {
    public String getName();
    public byte[] getContent();
    public byte[] getChecksum();
}
