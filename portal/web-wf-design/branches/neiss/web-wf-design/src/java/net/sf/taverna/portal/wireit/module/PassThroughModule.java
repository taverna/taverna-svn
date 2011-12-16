package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputFirer;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * This module has two funtions, save the value to pass back to WireIt and pass the value to the next module.
 * <p>
 * This same module can be use for URL, Baclava and String pass throughs and any other passthrough 
 *    as long at Object type is understood by the JSONObject constructor.
 * @author Christian
 */
public class PassThroughModule extends Module implements OutputListener{
        
    /** Handles the firing of output ready to connected OutputListeners */
    OutputFirer outputFirer;
    
   /**
     * Constructor for passing the json to the super class, and creating and OutputFirer.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public PassThroughModule (JSONObject json) throws JSONException{
        super(json);
        outputFirer = new OutputFirer();
    }
    
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        //Do nothing reacts to push not run()
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals("input")){
            return this;
        } else {
            throw new JSONException("Unsupported port name: " + terminal);
        }
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (terminal.equals("output")){
            outputFirer.addOutputListener(listener);
        } else {
            throw new JSONException("Unsupported port name: " + terminal);
        }
    }

    @Override
    /**
     * Saves the value and passes it on to any downStream modules.
     * 
     * @param output Information being passed from one module to another.
     * @param outputBuilder Logging buffer. 
     * @throws WireItRunException Something has gone wrong. This could be caused by exectution 
     *    or even one of the downstream modules.
     */
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
        values.put("both", output);
        outputFirer.fireOutputReady(output, outputBuilder);
    }
}
