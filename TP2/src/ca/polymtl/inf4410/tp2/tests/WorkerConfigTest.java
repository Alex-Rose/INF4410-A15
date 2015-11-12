package ca.polymtl.inf4410.tp2.tests;

import ca.polymtl.inf4410.tp2.worker.WorkerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Alexandre on 11/12/2015.
 */
public class WorkerConfigTest {

    private String filePath;

    @Before
    public void setUp() throws Exception {
        filePath = "worker.test.xml";
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
        WorkerConfig config = new WorkerConfig();

        config.setName("ComputationUnit");
        config.setPort(5047);
        config.setCapacity(1000);
        config.setMalice(2);

        WorkerConfig config2 = new WorkerConfig();

        config2.setName("ComputationUnit");
        config2.setPort(5047);
        config2.setCapacity(1000);
        config2.setMalice(2);

        assertTrue(config.equals(config2));
    }

    @Test
    public void testGetName() throws Exception {
        WorkerConfig config = new WorkerConfig();
        config.setName("workR");
        assertEquals("workR", config.getName());
    }

    @Test
    public void testGetPort() throws Exception {
        WorkerConfig config = new WorkerConfig();
        int port = 456;
        config.setPort(port);
        assertEquals(port, config.getPort());
    }

    @Test
    public void testGetCapacity() throws Exception {
        WorkerConfig config = new WorkerConfig();
        int cap = 4561;
        config.setCapacity(cap);
        assertEquals(cap, config.getCapacity());
    }

    @Test
    public void testGetMalice() throws Exception {
        WorkerConfig config = new WorkerConfig();
        int malice = 45612;
        config.setMalice(malice);
        assertEquals(malice, config.getMalice());
    }

    @Test
    public void testReadWrite() throws Exception {
        WorkerConfig config = new WorkerConfig();

        config.setName("ComputationUnit");
        config.setPort(5047);
        config.setCapacity(1000);
        config.setMalice(2);
        config.writeToFile(filePath);

        WorkerConfig cfg2 = WorkerConfig.readFromFile(filePath);
        assertTrue(config.equals(cfg2));
    }
}