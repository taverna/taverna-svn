package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Input module for a depth 0 port where inputs are provided as a String.
 * <p>
 * Because tavern can handle a single value to a depth 1 port 
 *    it is allowed to connect this modules to a depth 1 Listener.
 * <p>
 * The only role of this module is to pass the values received from WireIt to the downstream modules.
 * @author Christian
 */
public class InputStringModule extends InputModule{
        
    /**
     * Constructor for passing the json to the super class.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public InputStringModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    /**
     * Passes the value to any listeners.
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        Object value = values.get(PORT_NAME);
        output.fireOutputReady(value, outputBuilder);
    }

}
