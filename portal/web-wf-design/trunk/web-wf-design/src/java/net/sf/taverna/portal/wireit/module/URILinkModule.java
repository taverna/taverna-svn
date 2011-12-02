/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.utils.DelimiterURI;
import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.net.URI;
import java.net.URISyntaxException;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputFirer;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * This class handles all modules where the only value is a uri.
 * <p>
 * This could be an Input, and Output or a passthrough module as all have just one "uri" value.
 * <p>
 * This could be a straight URI or a Baclava Module as both have just one "uri" value.
 * 
 * @author Christian
 */
public class URILinkModule extends Module implements OutputListener{

    /** Handles the firing of output (if any), ready to connected OutputListeners */
    private OutputFirer outputFirer;    
    
    /** The expected name of any output port */
    private final String OUTPUT_PORT_NAME = "output";
    
    /** The expected name of any input port */    
    private final String INPUT_PORT_NAME = "input";
    
    /** The expected name of the value, passed from and to WireIt */
    private final String VALUE_SAVE_NAME = "uri";
            
    /** Flag to let run method know if it shoudl execute or if outputReady will cause execution. */
    private boolean expectingInput;

    /**
     * Construct the module by calling super constructor and setting up output firer.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public URILinkModule (JSONObject json) throws JSONException{
        super(json);
        outputFirer = new OutputFirer();
        expectingInput = false;
    }

    //@Override
     /**
     * Returns an OutputLister for the input ports.
     * <p>
     * The assumption is that this method is only called if the module is being wired to an upstream module.
     * This results in the module wiating for outputReady to be called rather than executing on the run method.
     * <p>
     * See Wiring.java for more details.
     * 
     * @param terminal Name of the Input port to be attached.
     * @return The Listener that will handles the Object coming in.
     * @throws JSONException It is WireIt's responsibility that the "wires" array correctly matches the "modules" array.
     *    If this is not the case an exception is thrown.
     */
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals(INPUT_PORT_NAME)){
            expectingInput = true;
            return this;
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected input");
        }
    }


    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (terminal.equals(OUTPUT_PORT_NAME)){
            outputFirer.addOutputListener(listener);
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected output");
        }
    }

    @Override
    /** 
     * Where suitable this method will cause the module to be run.
     * <p>
     * There are two ways to trigger a module to run.
     * <p>
     * If getOutputListener() was not called this method will begin execution on this command.
     *    Typically these are Input modules.
     * <p>
     * When a module is run it must pass any output to any module listening on any of its output terminals.
     * <p>
     * If getOutputListener() was not called this method returns with no action taken.
     * 
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        if (expectingInput) {
            //Don't run here but run when input arrives.
            return;
        }
        Object value = values.get(VALUE_SAVE_NAME);
        //Only run if there is a value set.
        if (value!= null) {
            try {
                URI uri = new URI(value.toString());
                outputFirer.fireOutputReady(uri, outputBuilder);
            } catch (URISyntaxException ex) {
                throw new WireItRunException("Ilegal URI: " + value, ex);
            }
        }   
    }
 
    @Override
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException {
        if (output instanceof DelimiterURI){
            DelimiterURI delimiterURI = (DelimiterURI)output;
            values.put(VALUE_SAVE_NAME, delimiterURI.getURI()); 
            outputFirer.fireOutputReady(delimiterURI.getURI(), outputBuilder);
        } else {
            values.put(VALUE_SAVE_NAME, output);
            outputFirer.fireOutputReady(output, outputBuilder);
        }
    }
 
}
