package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.utils.DelimiterURI;
import net.sf.taverna.portal.wireit.exception.WireItRunException;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * Module to receive the output from Upstream Modules and return these to wireIt.
 * <p>
 * This module is only expected to save any inputs.
 * <p>
 * WARNING: This is a prototype so does not yet handle all possible input types. Please Exstend accordingly.
 * @author Christian
 */
public class OutputModule extends Module implements OutputListener{
       
    /** The expected name of the input port */
    final String PORT_NAME = "input";
    
    /**
     * Constructor for passing the json to the super class.
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public OutputModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    @Override
    /**
     * Does nothing as this module depends on input received from other modules.
     */
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        //Do nothing reacts to push not run()
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals(PORT_NAME)){
            return this;
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected input");
        }
    }

    /**
     * Should never be called as module has no output (to other modules).
     * 
     * @param terminal
     * @return
     * @throws JSONException Always thrown as module has no output (to other modules).
     */
    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        throw new JSONException("Module OutputPort has no output ports");
    }

    @Override
    /**
     * Passes the Object on to any downstream modules.
     * 
     * WARNING: This is a prototype so does not yet handle all possible input types. Please Exstend accordingly.
     *    This will only cause an problem if the Object is a type which can not be handled corrently 
     *        by the JSONObject constructor.
     * @param outputBuilder Logging buffer. 
     * @throws  WireItRunException Not thrown by this method but is thrown by superclass versions.
     */
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
        if (output instanceof DelimiterURI){
             DelimiterURI delimiterURI = (DelimiterURI)output;
             values.put(PORT_NAME, delimiterURI.getURI());                
        } else if (output instanceof byte[]){
             byte[] array = (byte[])output;
             String asString = new String(array);
            values.put(PORT_NAME, asString);
        } else {
            values.put(PORT_NAME, output);
        }
    }
}
