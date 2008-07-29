package net.sf.taverna.matserver;

import java.util.Map;
import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author petarj
 */
public class MatEngineImplTest extends TestCase {

    MatEngine engine;

    @Override
    protected void setUp() throws Exception {
        this.engine = new MatEngineImpl();
    }

    @Test
    public void testExecSctipt() {
        MatArray X = new MatArray();
        X.setType(MatArray.DOUBLE_TYPE);
        int[] dims = new int[2];
        dims[0] = dims[1] = 1;
        X.setDimensions(dims);

        double[] dre = new double[1];
        dre[0] = 4;
        X.setDoubleDataRe(dre);
        engine.setVar("X", X);

        MatArray S = new MatArray();
        S.setType(MatArray.CHAR_TYPE);
        S.setCharData(new String[]{
                    "Hello world", "Ehlo..."
                });
        S.setDimensions(new int[]{
                    2, 11
                });
        engine.setVar("S", S);

        String[] names = new String[2];
        names[0] = "Y";
        names[1] = "S";
        engine.setOutputNames(names);
        engine.execute("Y=magic(X);");
        Map<String, MatArray> outs = engine.getOutputVars();
        MatArray Y = outs.get("Y");
        MatArray SO = outs.get("S");

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

        printArr(S.getCharData());
        printArr(SO.getCharData());
        assertTrue(Arrays.equals(S.getCharData(), SO.getCharData()));

    }

    private void printArr(Object[] oar) {
        System.out.println("Array print:");
        for (int i = 0; i < oar.length; i++) {
            System.out.println("[" + oar[i].toString() + "]");
        }
        System.out.println("-----------------------------------");
    }
}
