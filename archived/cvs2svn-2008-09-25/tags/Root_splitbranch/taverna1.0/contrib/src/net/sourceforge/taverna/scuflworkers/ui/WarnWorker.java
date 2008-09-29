package net.sourceforge.taverna.scuflworkers.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor displays a warning message to the user.  It should only be used
 * with interactive workflows that are being run from Taverna.  Server-side
 * or command-line workflows should not use this processor.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput message  The prompt message to be displayed.
 * @tavinput title    The title for the title-bar of the dialog window.
 * 
 * @tavinput answer   The response from the user.  This is a dummy value since 
 * 					  no response is allowed from the user.
 */
public class WarnWorker extends AskWorker implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        JOptionPane.showMessageDialog(null, 
                inAdapter.getString("message"),
                inAdapter.getString("title"),
                JOptionPane.WARNING_MESSAGE
                
               );
        outAdapter.putString("answer","answer");
        return outputMap;
    }


}
