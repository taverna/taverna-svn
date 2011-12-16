package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Input module for a depth 1 port where inputs are provided as a String values seperated by a newline.
 * <p>
 * The only role of this module is to pass the values received from WireIt to the downstream modules.
 * @author Christian
 */
public class InputListModule extends InputModule{
        
    /**
     * Constructor for passing the json to the super class.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public InputListModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    /**
     * Splits the value into an array of strings and passes this array to any listeners.
     * <p>
     * The Inpout "value" is assumed to be newline delimited, as that is what WireIt's textField does.
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        Object value = values.get(PORT_NAME);
        String[] tokens = value.toString().split("\\n");
        output.fireOutputReady(tokens, outputBuilder);
    }

}
