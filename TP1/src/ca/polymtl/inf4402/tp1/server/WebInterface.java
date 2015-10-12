package ca.polymtl.inf4402.tp1.server;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;

/**
 * Created by Alexandre on 9/24/2015.
 */
public class WebInterface {
    public static void main(String[] args) throws Exception
    {
        ServerSocket server = new ServerSocket(47000);
        Socket conn = server.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        ServerInterface stub = loadServerStub();

        // don't use buffered writer because we need to write both "text" and "binary"
        OutputStream out = conn.getOutputStream();
        int count = 0;
        while (true)
        {
            count++;
            String line = reader.readLine();
            if (line == null)
            {
                System.out.println("Connection closed");
                break;
            }
            System.out.println("" + count + ": " + line);
            if (line.equals(""))
            {
                System.out.println("Writing response...");

                String content = "<html><body>Liste des fichiers : <ul>";

                for (Iterator<String> iterator = stub.list().iterator(); iterator.hasNext();){
                    content += "<li>" + iterator.next() + "</li>";
                }
                content += "</ul></body></html>";

                // need to construct response bytes first
                byte [] response = content.getBytes("ASCII");

                String statusLine = "HTTP/1.1 200 OK\r\n";
                out.write(statusLine.getBytes("ASCII"));

                String contentLength = "Content-Length: " + response.length + "\r\n";
                out.write(contentLength.getBytes("ASCII"));

                // signal end of headers
                out.write( "\r\n".getBytes("ASCII"));

                // write actual response and flush
                out.write(response);
                out.flush();;
            }
        }
    }

    /**
     * Connect to RMI server and get server object handle of type ServerInterface
     * Server hostname defined by {@link ca.polymtl.inf4402.tp1.client.Client setServer}
     * @return ServerInterface
     */
    private static ServerInterface loadServerStub() {
        ServerInterface stub = null;
        String hostname = null;
        try {
            FileReader reader = new FileReader(".server");
            BufferedReader buffer = new BufferedReader(reader);
            String value = buffer.readLine();
            buffer.close();

            if (value.equals("local")){
                hostname = "127.0.0.1";
            }
            else {
                hostname = value;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Server configuration not found. Please run setServer");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }
}
