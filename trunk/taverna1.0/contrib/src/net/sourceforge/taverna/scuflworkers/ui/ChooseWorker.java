package net.sourceforge.taverna.scuflworkers.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor allows the user to select an option from a list of 
 * radio buttons.  It should only be used
 * with interactive workflows that are being run from Taverna.  Server-side
 * or command-line workflows should not use this processor.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 * 
 * @tavinput title				The title to be displayed in the dialog box's titlebar
 * @tavinput message			The prompt message to be displayed
 * @tavinput selectionValues    An array of values to be displayed for the radio buttons.
 * 
 * @tavoutput answer			The user's selection.
 */
public class ChooseWorker implements LocalWorker {
    
    public ChooseWorker(){
        
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        String[] valueList = inAdapter.getStringArray("selectionValues");
        
        JPanel panel = new JPanel();
        JRadioButton[] buttonArray = new JRadioButton[valueList.length];
        for (int i=0; i < valueList.length; i++){
            buttonArray[i] = new JRadioButton(valueList[i]);
        }
        
        int value = JOptionPane.showOptionDialog(null,  
         inAdapter.getString("message"),
         inAdapter.getString("title"),
         JOptionPane.OK_CANCEL_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         buttonArray,
         buttonArray[0]
         
        );
        
        outAdapter.putString("answer",buttonArray[value].getText());
        
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"title", "message","selectionValues"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'","l('text/plain')"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"answer"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
       return new String[]{"'text/plain'"};
    }

}
