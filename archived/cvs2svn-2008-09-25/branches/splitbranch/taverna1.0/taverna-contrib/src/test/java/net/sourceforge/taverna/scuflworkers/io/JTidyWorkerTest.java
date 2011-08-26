package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
public class JTidyWorkerTest extends TestCase {
    static final String LINE_ENDING = System.getProperty("line.separator");

    public void testExecute() throws Exception{
        LocalWorker worker = new JTidyWorker();
        
        
            // Create a URL for the desired page
            URL url = new URL("http://www.nature.com/index.html");
        
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = in.readLine()) != null) {
               sb.append(str);
               sb.append(LINE_ENDING);
            }
            in.close();
       


        
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("inputHtml", sb.toString());
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertNotNull("The outputMap was null", outputMap);
        
        String results = outAdapter.getString("results");
        
        
        assertFalse("The results were empty",results.equals(""));
        assertNotNull("The results were null",results);
                
        
        
    }

}
