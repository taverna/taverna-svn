package net.sf.taverna.matserver;

import java.util.Map;
import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author petarj
 */
public class MatEngineImplTest extends TestCase {

    MatEngine engine;

    @Before
    protected void setUp() throws Exception {
        engine = new MatEngineImpl();
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
        assertTrue(Arrays.equals(S.getCharData(), SO.getCharData()));
    //engine.clearVars();
    }

    @Test
    public void testDoubleArrays() {
        //Simple double real numbers matrix:
        MatArray ma = new MatArray();
        ma.setType(MatArray.DOUBLE_TYPE);
        ma.setDimensions(new int[]{3, 3});
        ma.setDoubleDataRe(new double[]{1, 4, 7, 2, 5, 8, 3, 6, 9});

        MatArray eye = new MatArray();
        eye.setType(MatArray.DOUBLE_TYPE);
        eye.setDimensions(new int[]{3, 3});
        eye.setDoubleDataRe(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1});

        //sparse matrix
        MatArray sparseMx = new MatArray();
        sparseMx.setType(MatArray.DOUBLE_TYPE);
        sparseMx.setDimensions(new int[]{10, 10});
        sparseMx.setDoubleDataRe(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        sparseMx.setRowIds(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        sparseMx.setColIds(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        sparseMx.setMaxNonZero(10);

        //multidimensional matrix
        MatArray mmx = new MatArray();
        mmx.setType(MatArray.DOUBLE_TYPE);
        mmx.setDimensions(new int[]{3, 3, 3});
        mmx.setDoubleDataRe(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27});

        //complex array
        MatArray cma = new MatArray();
        cma.setType(MatArray.DOUBLE_TYPE);
        cma.setDimensions(new int[]{3, 1});
        cma.setDoubleDataRe(new double[]{1, 2, 3});
        cma.setDoubleDataIm(new double[]{1, 2, 3});

        engine.setVar("ma", ma);
        engine.setVar("theEye", eye);
        engine.setVar("sparseMx", sparseMx);
        engine.setVar("cma", cma);
        engine.setVar("mmx", mmx);
        engine.setOutputNames(new String[]{"Y", "sparseMx", "spTest", "cma", "mmx"});

        String script = "Y=ma*theEye;\n" +
                "spTest=zeros(10,10);\n" +
                "for i=1:10\n" +
                "spTest(i,i)=i;\n" +
                "end;\n" +
                "spTest=sparse(spTest);";

        engine.execute(script);

        Map<String, MatArray> outs = engine.getOutputVars();

        MatArray Y = outs.get("Y");

        MatArray spTest = outs.get("spTest");
        MatArray spMx = outs.get("sparseMx");

        MatArray cplx = outs.get("cma");

        MatArray multiDimensionalMx = outs.get("mmx");

        assertNotNull(Y);
        assertNotNull(spTest);
        assertNotNull(spMx);
        assertNotNull(cma);
        assertNotNull(multiDimensionalMx);
        assertEquals(ma, Y);
        assertEquals(sparseMx, spMx);
        assertEquals(mmx, multiDimensionalMx);
        assertTrue(Arrays.equals(spTest.getRowIds(), spMx.getRowIds()));
        assertTrue(Arrays.equals(spTest.getColIds(), spMx.getColIds()));
        assertEquals(spTest, spMx);
        assertEquals(cma, cplx);

    }

    @Test
    public void testCharArrays() {
        MatArray charArray = new MatArray();
        charArray.setType(MatArray.CHAR_TYPE);
        charArray.setDimensions(new int[]{2, 3});
        charArray.setCharData(new String[]{"foo", "bar"});

        engine.setVar("foobar", charArray);
        engine.setOutputNames(new String[]{"foobar", "ft"});
        engine.execute("ft=foobar';");
        Map<String, MatArray> outs = engine.getOutputVars();

        MatArray caTransposed = new MatArray();
        caTransposed.setType(MatArray.CHAR_TYPE);
        caTransposed.setDimensions(new int[]{3, 2});
        caTransposed.setCharData(new String[]{"fb", "oa", "or"});

        MatArray ft = outs.get("ft");
        assertEquals(caTransposed, ft);

        MatArray charArrayOut = outs.get("foobar");
        assertEquals(charArrayOut, charArray);
    }

    @Test
    public void testCellArrays() {
        MatArray cellArray = new MatArray();
        cellArray.setType(MatArray.CELL_TYPE);
        cellArray.setDimensions(new int[]{2, 1});

        MatArray a = new MatArray();
        a.setType(MatArray.CHAR_TYPE);
        a.setDimensions(new int[]{1, 1});
        a.setCharData(new String[]{"a"});
        MatArray theAnswer = new MatArray();
        theAnswer.setType(MatArray.DOUBLE_TYPE);
        theAnswer.setDimensions(new int[]{1, 1});
        theAnswer.setDoubleDataRe(new double[]{42});

        cellArray.setCellData(new MatArray[]{a, theAnswer});

        engine.setVar("cellArray", cellArray);
        engine.setOutputNames(new String[]{"cellArray", "generated"});
        engine.execute("generated={'a';42}");
        Map<String, MatArray> outs = engine.getOutputVars();
        
        MatArray ca = outs.get("cellArray");
        assertNotNull(ca);
        assertEquals(cellArray, ca);

        MatArray generated = outs.get("generated");
        assertNotNull(generated);
        assertEquals(generated, cellArray);

//        System.out.println("cellArray:________________________\n" + cellArray.toString());
//        System.out.println("ca:_______________________________\n" + ca.toString());
//        System.out.println("generated:________________________\n" + generated.toString());

    }
}
