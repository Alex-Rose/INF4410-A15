package ca.polymtl.inf4410.tp2.tests;

import ca.polymtl.inf4410.tp2.master.Master;
import ca.polymtl.inf4410.tp2.master.MasterConfig;
import javafx.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Alexandre on 11/12/2015.
 */
public class MasterConfigTest {

    private String filePath;

    @Before
    public void setUp() throws Exception {
        filePath = "test.xml";
    }

    @After
    public void tearDown() throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testEquals() throws Exception {
        MasterConfig config = new MasterConfig();

        ArrayList<Pair<String, String>> servers = new ArrayList<Pair<String, String>>();
        servers.add(new Pair<>("test1", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));

        config.setSafeMode(false);
        config.setConfigFile("master.xml");
        config.setOperationFile("lol.op");
        config.setServers(servers);
        config.setRmiPort(1099);

        MasterConfig config2 = new MasterConfig();

        config2.setSafeMode(false);
        config2.setConfigFile("master.xml");
        config2.setOperationFile("lol.op");
        config2.setServers(servers);
        config2.setRmiPort(1099);

        assertTrue(config.equals(config2));
    }

    @Test
    public void testIsSafeMode() throws Exception {
        MasterConfig config = new MasterConfig();
        config.setSafeMode(false);
        assertFalse(config.isSafeMode());

        config.setSafeMode(true);
        assertTrue(config.isSafeMode());
    }

    @Test
    public void testGetOperationFile() throws Exception {
        MasterConfig config = new MasterConfig();
        String path = "operation.file\\test.txt";
        config.setOperationFile(path);
        assertEquals(path, config.getOperationFile());
    }

    @Test
    public void testGetServers() throws Exception {
        MasterConfig config = new MasterConfig();
        ArrayList<Pair<String, String>> servers = new ArrayList<Pair<String, String>>();
        servers.add(new Pair<>("test1", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));

        config.setServers(servers);
        assertArrayEquals(servers.toArray(), config.getServers().toArray());
    }

    @Test
     public void testGetConfigFile() throws Exception {
        MasterConfig config = new MasterConfig();
        String path = "config.file\\test.txt";
        config.setConfigFile(path);
        assertEquals(path, config.getConfigFile());
    }

    @Test
    public void testGetRmiPort() throws Exception {
        MasterConfig config = new MasterConfig();
        int port = 1091;
        config.setRmiPort(port);
        assertEquals(port, config.getRmiPort());
    }

    @Test
    public void testReadWrite() throws Exception {
        MasterConfig config = new MasterConfig();

        ArrayList<Pair<String, String>> servers = new ArrayList<Pair<String, String>>();
        servers.add(new Pair<>("test1", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));
        servers.add(new Pair<>("test2", "Worker"));

        config.setSafeMode(false);
        config.setConfigFile("master.xml");
        config.setOperationFile("lol.op");
        config.setServers(servers);
        config.setRmiPort(1099);
        config.writeToFile(filePath);

        MasterConfig cfg2 = MasterConfig.readFromFile(filePath);
        assertTrue(config.equals(cfg2));
    }
}