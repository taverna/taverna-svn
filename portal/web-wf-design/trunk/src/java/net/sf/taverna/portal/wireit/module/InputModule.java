package net.sf.taverna.portal.wireit.module;

import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputFirer;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * Base module for any module that purely provides input to other modules.
 * <p>
 * The only role of this module is to pass the values received from WireIt to the downstream modules.
 * @author Christian
 */
public abstract class InputModule extends Module{
        
    /** Handles the firing of output ready to connected OutputListeners */
    OutputFirer output;
    
    /** The expected name of the output port */
    final String PORT_NAME = "output";

    /**
     * Constructor for passing the json to the super class, and creating and OutputFirer.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public InputModule (JSONObject json) throws JSONException{
        super(json);
        output = new OutputFirer();
    }
       
    /**
     * Should never be called as comments have no inputs
     * 
     * @param terminal
     * @return
     * @throws JSONException Always thrown as class has no inputs.
     */
    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        throw new JSONException("InputModule has no Inputs");
    }

    /**
     * Adds an output listener for this module.
     * 
     * @param terminal Must be "output" or and exception is thrown.
     * @param listener Listener to be used.
     * @throws JSONException It is WireIt's responsibility that the "wires" array correctly matches the "modules" array.
     *    If this is not the case an exception is thrown.
     */
    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (terminal.equals(PORT_NAME)){
            output.addOutputListener(listener);
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected output");
        }
    }

}
