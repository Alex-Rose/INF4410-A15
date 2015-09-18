package ca.polymtl.inf4402.tp1.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Alexandre on 9/18/2015.
 */


public class FileHelper {

    /**
     * @param path URI to file being read
     * @return     Byte array of the file's content
     */
    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeFile(String path, byte[] data){
        File file = new File(path);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data);
            stream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getChecksum(byte[] data){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.digest(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
