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
 * Helper for local file IO
 * Created by Alexandre on 9/18/2015.
 */
public class FileHelper {

    /**
     * @param path URI to file being read
     * @return     Byte array of the file's content, null of file not found
     */
    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write bytes to file. Will overwrite any existing file
     * @param path file to write
     * @param data file content
     * @return true if writing is a success
     */
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

    /**
     * Get checksum of a given byte array
     * @param data array to hash
     * @return MD5 hash
     */
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

    /**
     * Get checksum of a given file
     * @param path file to get hash from
     * @return MD5 hash of file or [-1] if file not found
     */
    public static byte[] getChecksum(String path){
        try {
            byte[] data = Files.readAllBytes(Paths.get(path));
            return getChecksum(data);
        } catch (IOException e) {
            return new byte[]{-1};
        }
    }
}
