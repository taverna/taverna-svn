package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class ConcatenateFileListWorkerTest extends TestCase {

    public void testExecute() throws Exception{
    	
    	System.out.println("ConcatenateFileListWorkerTest.testExecute() skipped");
    	
    	//FAILS - windows specific path c:/....
    	
//        LocalWorker worker = new ConcatenateFileListWorker();
//        String[] filelist= new String[]{
//                "C:/Documents and Settings/Mark/My Documents/projects/taverna1.0/contrib/test/src/etc/concatenateTestFile1.txt",
//                "C:/Documents and Settings/Mark/My Documents/projects/taverna1.0/contrib/test/src/etc/concatenateTestFile2.txt"
//        };
//        String outFile = "C:/Documents and Settings/Mark/My Documents/projects/taverna1.0/contrib/test/src/etc/concatenateTestOut.txt";
//        
//        Map inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        inAdapter.putStringArray("filelist", filelist);
//        inAdapter.putString("outputfile",outFile);
//        inAdapter.putBoolean("displayresults",true);
//        
//        Map outputMap = worker.execute(inputMap);
//        assertNotNull("The output map was null",outputMap);
//        
//        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
//        
//        
//        String results = outAdapter.getString("results");
//        assertTrue("The output map does not contain the results",outputMap.containsKey("results"));
//        assertNotNull("The results were null",results);
//        
//        String file1 = readFile(filelist[0]);
//        String file2 = readFile(filelist[1]);
//        String concatenatedFile = readFile(outFile);
//        
//        assertTrue("The files were not concatenated properly",concatenatedFile.equals(file1+file2) );
        
    }
    
    private String readFile(String file) throws Exception{
        StringBuffer sb = new StringBuffer(2000);
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            in.close();
        } catch (IOException e) {
        }
        return sb.toString();
    }

}
