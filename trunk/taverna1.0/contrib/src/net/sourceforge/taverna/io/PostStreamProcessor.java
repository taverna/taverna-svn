package net.sourceforge.taverna.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class reads the text from a socket and places it in the outputmap.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class PostStreamProcessor implements StreamProcessor {

    public static final String NEWLINE = System.getProperty("line.separator");
    
    public PostStreamProcessor(){
        
    }

    /**
     * @see net.sourceforge.taverna.io.StreamProcessor#processStream(java.io.InputStream)
     */
    public Map processStream(InputStream stream) throws IOException {
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        StringBuffer sb = new StringBuffer(2000);
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
        
            String str;
            
            while ((str = rd.readLine()) != null) {
                sb.append(str);
                sb.append(NEWLINE);
            }
            rd.close();
        } catch (IOException e) {
        }
        
        outAdapter.putString("outputText", sb.toString());
        
        
        return outputMap;
    }

}
