package net.sf.taverna.matserver;

import java.util.Map;
import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO check infinite and NaNs
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
        double[] expectedMagic4 = new double[]{16, 5, 9, 4, 2, 11, 7, 14, 3, 10,
            6, 15, 13, 8, 12, 1
        };

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
        mmx.setDoubleDataRe(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                    13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27
                });

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
        engine.setOutputNames(new String[]{"Y", "sparseMx", "spTest", "cma",
                    "mmx"
                });

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

    @Test
    public void testLogicalArrays() {
        MatArray logicalArray = new MatArray();
        logicalArray.setType(MatArray.LOGICAL_TYPE);
        logicalArray.setDimensions(new int[]{3, 3});
        logicalArray.setLogicalData(new boolean[]{false, false, true, true,
                    false, true, false, true, true
                }); //game of life glider

        engine.setVar("glider", logicalArray);
        engine.setOutputNames(new String[]{"glider", "invGlider"});

        engine.execute("invGlider=~glider");

        Map<String, MatArray> outs = engine.getOutputVars();
        MatArray gliderFromEngine = outs.get("glider");
        MatArray invGlider = outs.get("invGlider");

        MatArray expectedInvGlider = new MatArray();
        expectedInvGlider.setType(MatArray.LOGICAL_TYPE);
        expectedInvGlider.setDimensions(new int[]{3, 3});
        expectedInvGlider.setLogicalData(new boolean[]{true, true, false, false,
                    true, false, true, false, false
                });

        System.out.println("logarr:______________________________________________________________________________\n" +
                logicalArray);
        System.out.println(
                "\n___________________________________________________________________________________\n");
        System.out.println("gliderFromEngine:____________________________________________________________________\n" +
                gliderFromEngine);
        System.out.println(
                "\n___________________________________________________________________________________\n");

        assertEquals(logicalArray, gliderFromEngine);
        assertEquals(expectedInvGlider, invGlider);

    }

    @Test
    public void testSingleArrays() {
        MatArray singleArray = new MatArray();
        singleArray.setType(MatArray.SINGLE_TYPE);
        singleArray.setDimensions(new int[]{1, 1});
        singleArray.setSingleDataRe(new float[]{42.2f});

        engine.setVar("input", singleArray);
        engine.setOutputNames(new String[]{"input", "theAnswer"});

        engine.execute("theAnswer=single(double(input)-0.2);");

        Map<String, MatArray> outs = engine.getOutputVars();

        MatArray fromEngine = outs.get("input");
        MatArray theAnswer = outs.get("theAnswer");
        MatArray expectedAns = new MatArray();
        expectedAns.setType(MatArray.SINGLE_TYPE);
        expectedAns.setDimensions(new int[]{1, 1});
        expectedAns.setSingleDataRe(new float[]{42.0f});

        System.out.println("input:\n" + singleArray);
        System.out.println(
                "\n___________________________________________________________________________________\n");
        System.out.println("fromEngine:\n" + fromEngine);
        System.out.println(
                "\n___________________________________________________________________________________\n");
        System.out.println("theAnswer:\n" + theAnswer);
        System.out.println(
                "\n___________________________________________________________________________________\n");

        assertEquals(singleArray, fromEngine);
        assertEquals(expectedAns, theAnswer);

    }

    @Test
    public void testStructArrays() {    //TODO: add abstraction layers to make it less tedious...
        MatArray sentientComputers = new MatArray();
        sentientComputers.setType(MatArray.STRUCT_TYPE);
        sentientComputers.setDimensions(new int[]{1, 2});
        sentientComputers.setFieldNames(new String[]{"name", "song",
                    "someNumber", "someBool"
                });

        MatArray names = new MatArray();
        names.setType(MatArray.CELL_TYPE);
        names.setDimensions(new int[]{1, 2});

        MatArray name1 = new MatArray();
        name1.setType(MatArray.CHAR_TYPE);
        name1.setDimensions(new int[]{1, 7});
        name1.setCharData(new String[]{"HAL9000"});

        MatArray name2 = new MatArray();
        name2.setType(MatArray.CHAR_TYPE);
        name2.setDimensions(new int[]{1, 12});
        name2.setCharData(new String[]{"Deep Thought"});

        names.setCellData(new MatArray[]{name1, name2});

        MatArray songs = new MatArray();
        songs.setType(MatArray.CELL_TYPE);
        songs.setDimensions(new int[]{1, 2});

        MatArray song1 = new MatArray();
        song1.setType(MatArray.CHAR_TYPE);
        song1.setDimensions(new int[]{1, 10});
        song1.setCharData(new String[]{"daisy bell"});

        songs.setCellData(new MatArray[]{song1, null});

        MatArray numbers = new MatArray();
        numbers.setType(MatArray.CELL_TYPE);
        numbers.setDimensions(new int[]{1, 2});

        MatArray number1 = new MatArray();
        number1.setType(MatArray.DOUBLE_TYPE);
        number1.setDimensions(new int[]{1, 1});
        number1.setDoubleDataRe(new double[]{9000});

        MatArray number2 = new MatArray();
        number2.setType(MatArray.DOUBLE_TYPE);
        number2.setDimensions(new int[]{1, 1});
        number2.setDoubleDataRe(new double[]{42});

        numbers.setCellData(new MatArray[]{number1, number2});

        MatArray booleansArray = new MatArray();
        booleansArray.setType(MatArray.CELL_TYPE);
        booleansArray.setDimensions(new int[]{1, 2});

        MatArray b1 = new MatArray();
        b1.setType(MatArray.LOGICAL_TYPE);
        b1.setDimensions(new int[]{1, 1});
        b1.setLogicalData(new boolean[]{true});

        MatArray b2 = new MatArray();
        b2.setType(MatArray.LOGICAL_TYPE);
        b2.setDimensions(new int[]{1, 1});
        b2.setLogicalData(new boolean[]{false});

        booleansArray.setCellData(new MatArray[]{b1, b2});

        sentientComputers.setCellData(new MatArray[]{names, songs, numbers,
                    booleansArray
                });

        engine.setVar("scs", sentientComputers);
        String script = "GLaDOS=struct('name','GLaDOS','song','still alive','someNumber',23,'someBool',logical(0));";
        engine.setOutputNames(new String[]{"scs", "GLaDOS"});
        engine.execute(script);
        Map<String, MatArray> outs = engine.getOutputVars();

        MatArray scsFromEngine = outs.get("scs");
        assertEquals(sentientComputers, scsFromEngine);

        MatArray expGlados = new MatArray();
        expGlados.setType(MatArray.STRUCT_TYPE);
        expGlados.setDimensions(new int[]{1, 1});
        expGlados.setFieldNames(new String[]{"name", "song", "someNumber",
                    "someBool"
                });

        MatArray ns = new MatArray();
        ns.setType(MatArray.CELL_TYPE);
        ns.setDimensions(new int[]{1, 1});

        MatArray name = new MatArray();
        name.setType(MatArray.CHAR_TYPE);
        name.setDimensions(new int[]{1, 6});
        name.setCharData(new String[]{"GLaDOS"});

        ns.setCellData(new MatArray[]{name});

        MatArray ss = new MatArray();
        ss.setType(MatArray.CELL_TYPE);
        ss.setDimensions(new int[]{1, 1});

        MatArray song = new MatArray();
        song.setType(MatArray.CHAR_TYPE);
        song.setDimensions(new int[]{1, 11});
        song.setCharData(new String[]{"still alive"});

        ss.setCellData(new MatArray[]{song});

        MatArray sns = new MatArray();
        sns.setType(MatArray.CELL_TYPE);
        sns.setDimensions(new int[]{1, 1});

        MatArray someNumber = new MatArray();
        someNumber.setType(MatArray.DOUBLE_TYPE);
        someNumber.setDimensions(new int[]{1, 1});
        someNumber.setDoubleDataRe(new double[]{23});

        sns.setCellData(new MatArray[]{someNumber});

        MatArray sbs = new MatArray();
        sbs.setType(MatArray.CELL_TYPE);
        sbs.setDimensions(new int[]{1, 1});

        MatArray someBool = new MatArray();
        someBool.setType(MatArray.LOGICAL_TYPE);
        someBool.setDimensions(new int[]{1, 1});
        someBool.setLogicalData(new boolean[]{false});
        sbs.setCellData(new MatArray[]{someBool});

        expGlados.setCellData(new MatArray[]{ns, ss, sns, sbs});

        MatArray glados = outs.get("GLaDOS");
        assertEquals(expGlados, glados);

    }
}
