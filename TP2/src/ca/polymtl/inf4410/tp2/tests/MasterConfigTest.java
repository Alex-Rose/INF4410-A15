package ca.polymtl.inf4410.tp2.tests;

import ca.polymtl.inf4410.tp2.master.Master;
import ca.polymtl.inf4410.tp2.master.MasterConfig;
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

        ArrayList<String> servers = new ArrayList<String>();
        servers.add("test1");
        servers.add("test2");
        servers.add("test2");

        config.setSafeMode(false);
        config.setConfigFile("file.xml");
        config.setOperationFile("lol.op");
        config.setServers(servers);

        MasterConfig config2 = new MasterConfig();

        config2.setSafeMode(false);
        config2.setConfigFile("file.xml");
        config2.setOperationFile("lol.op");
        config2.setServers(servers);

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
        ArrayList<String> servers = new ArrayList<String>();
        servers.add("test1");
        servers.add("test2");
        servers.add("test2");
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
    public void testReadWrite() throws Exception {
        MasterConfig config = new MasterConfig();

        ArrayList<String> servers = new ArrayList<String>();
        servers.add("test1");
        servers.add("test2");
        servers.add("test2");

        config.setSafeMode(false);
        config.setConfigFile("file.xml");
        config.setOperationFile("lol.op");
        config.setServers(servers);
        config.writeToFile(filePath);

        MasterConfig cfg2 = MasterConfig.readFromFile(filePath);
        assertTrue(config.equals(cfg2));
    }
}