package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class reads a delimited text file.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class DelimitedFileReader implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inputAdapter = new DataThingAdapter(inputMap);
        String fileurl = inputAdapter.getString("fileurl");
        
        String delimiter = inputAdapter.getString("delimiter");
        
        Map outputMap = new HashMap();
        DataThingAdapter outputAdapter = new DataThingAdapter(outputMap);
        
        ArrayList dataArray = new ArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileurl));
            String str;
            String lineEnding = System.getProperty("line.separator");
            
            String[] rowArray = null;
            ArrayList rowList = new ArrayList();
            int rowCount = 0;
            int numCols = 0;
            while ((str = in.readLine()) != null) {
               rowArray = str.split(delimiter);               
               rowList = (ArrayList)Arrays.asList(rowArray);              
            }
            in.close();
        } catch (IOException e) {
            throw new TaskExecutionException(e);
        }
        outputAdapter.putArrayList("dataArray", dataArray);
        
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"fileurl","delimiter"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"dataArray"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
       return new String[]{"l(l('text/plain'))"};
    }

}
