package ca.polymtl.inf4402.tp1.server;

import ca.polymtl.inf4402.tp1.shared.RemoteFile;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Alexandre on 9/16/2015.
 */
public class ServerFile implements RemoteFile, Serializable {

    protected String name;
    protected byte[] content;
    protected byte[] checksum;

    public ServerFile() {
        initEmpty();
    }

    public ServerFile(String name) {
        this.name = name;
        initEmpty();
    }

    public ServerFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public byte[] getChecksum() {
        return checksum;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setContent(byte[] content){
        this.content = content;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.digest(content);
            this.checksum = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode(){
        return name.hashCode() * checksum.hashCode();
    }

    private void initEmpty(){
        content = new byte[0];
        checksum = new byte[0];
    }
}
