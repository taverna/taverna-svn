package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class merges an input map of objects with a Velocity template
 * and outputs the results to an output file.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class VelocityFileWriter implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inputAdapter = new DataThingAdapter(inputMap);
        Map outputMap = new HashMap();
        DataThingAdapter outputAdapter = new DataThingAdapter(outputMap);
                
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
   
        String outputFile = inputAdapter.getString("outputFile");
        if (outputFile == null){
            throw new TaskExecutionException("The outputFile attribute was null");
        }
        
        String templateFile = inputAdapter.getString("templateFile");
        if (templateFile == null){
            throw new TaskExecutionException("The templateFile attribute was null");
        }

        try {
            ve.init();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            Template template = ve.getTemplate(templateFile);
            VelocityContext context = new VelocityContext();
            Iterator it = inputMap.keySet().iterator();
            String currKey = null;
            
            while(it.hasNext()){
                currKey = (String)it.next();
                context.put(currKey, inputAdapter.getString(currKey));
                
            }
            template.merge( context, writer );
            
        } catch (ResourceNotFoundException e1) {
           
            e1.printStackTrace();
        } catch (ParseErrorException e1) {
            
            e1.printStackTrace();
        } catch (Exception e1) {
            
            e1.printStackTrace();
        }
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"outputFile","template"}; //TODO figure out how to add a map of values here.
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
        return new String[]{""};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        // TODO Auto-generated method stub
        return null;
    }

}
