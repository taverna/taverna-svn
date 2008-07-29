package net.sf.taverna.matserver;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author petarj
 */
public class MarServerTest {

    private MatServer matServer;

    public MarServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGeneralFunctionality() throws MalformedURLException, Exception {
        try {
            MatServer mserv = new MatServer();
            mserv.start();
            ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
            Service serviceModel = serviceFactory.create(MatEngine.class);

            XFireProxyFactory proxyFactory = new XFireProxyFactory();
            MatEngine engine;

            try {
                engine = (MatEngine) proxyFactory.create(serviceModel, "http://localhost:8194/MatEngine");
            } catch (MalformedURLException ex) {
                Logger.getLogger(MarServerTest.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
            //--------------------------------------------------copy
            MatArray X = new MatArray();
            X.setType(MatArray.DOUBLE_TYPE);
            int[] dims = new int[2];
            dims[0] = dims[1] = 1;
            X.setDimensions(dims);

            double[] dre = new double[1];
            dre[0] = 4;
            X.setDoubleDataRe(dre);
            engine.setVar("X1", X);
            
            MatArray S = new MatArray();
            S.setType(MatArray.CHAR_TYPE);
            S.setCharData(new String[]{
                        "Hello world", "Ehlo..."
                    });
            S.setDimensions(new int[]{
                        2, 11
                    });
            engine.setVar("S1", S);

            String[] names = new String[2];
            names[0] = "Y1";
            names[1] = "S1";
            engine.setOutputNames(names);
            engine.execute("Y1=magic(X1);");
            Map<String, MatArray> outs = engine.getOutputVars();
            MatArray Y = outs.get("Y1");
            MatArray SO = outs.get("S1");

            assertEquals(Y.getType(), MatArray.DOUBLE_TYPE);
            assertNotNull(SO);
            double[] pr = Y.getDoubleDataRe();
            /*
            16     2     3    13
            5    11    10     8
            9     7     6    12
            4    14    15     1
             */
            double[] expectedMagic4 = new double[]{16, 5, 9, 4, 2, 11, 7, 14, 3, 10, 6, 15, 13, 8, 12, 1};

            assertTrue(Arrays.equals(pr, expectedMagic4));
            assertTrue(Arrays.equals(S.getCharData(), SO.getCharData()));
            mserv.stop();
        //----------------------------------------------------------------
        } catch (Exception exc) {
            Logger.getLogger(MarServerTest.class.getName()).log(Level.SEVERE, null, exc);
            throw exc;
        }
    }
}