package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * Base class for all modules.
 * <p>
 * Handles the storing of the "name", "config" and "value".
 * <p>
 * Because the base stores the three values it is typically able to recreate the json at the end 
 *    without help from the super classes.
 * 
 * @author Christian
 */
public abstract class Module {
    
    /** The name give by WireIt to the Module. 
     *  The name is store to be included in the final json.
     */
    String name;
    /**
     * Json representation of any configuration parameters.
     * Configuration is typically not changed by executing the module so the json format is stored.
     * Modules that require extra configuation details will extract them. 
     */
    JSONObject config;
    /**
     * The "value" object is actually a map of value names to values.
     * These are stored in a Hashmap so they can be updated as required by the sub classes.
     * This is only a store of the values included in the Json object to represent the pipe in WireIt.
     *     It only includes values received from or passed to terminals, if these have been set or will shwon in WireIt.
     */
    HashMap <String, Object> values;
    
    /**
     * Base constructor which stores the "name", "config" and "value".
     * <p>
     * "config" is just saved as json, leaving any config based construction to the sub classes.
     * <p>
     * The values are stored in a hashmap so are readly for subclasses to update during execution.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    Module (JSONObject json) throws JSONException{
        name = json.getString("name");
        config = json.getJSONObject("config");
        Object valuesObject = json.get("value");
        values = new HashMap <String, Object>();
        if (valuesObject instanceof JSONObject){
            JSONObject valuePair = (JSONObject) valuesObject;
            Iterator keys = valuePair.keys();
            while (keys.hasNext()){
                String key = (String)keys.next();
                String value = valuePair.getString(key);
                values.put(key, value);
            }
        } else {
            throw new JSONException ("Unexpected value type " + valuesObject.getClass());
        }
    }
    
    /**
     * All modules must return an OutputLister for any of their input ports.
     * <p>
     * See Wiring.java for more details.
     * 
     * @param terminal Name of the Input port to be attached.
     * @return The Listener that will handles the Object coming in.
     * @throws JSONException It is WireIt's responsibility that the "wires" array correctly matches the "modules" array.
     *    If this is not the case an exception is thrown.
     */
    public abstract OutputListener getOutputListener(String terminal) throws JSONException;

    /**
     * Adds the Listener as one of the downstream modules to this one.
     * 
     * Most Modules will implement this by having an OutputFirer associated with each output terminal.
     * 
     * @param terminal Name of the output port to be attached.
     * @param listener Listener to be used.
     * @throws JSONException It is WireIt's responsibility that the "wires" array correctly matches the "modules" array.
     *    If this is not the case an exception is thrown.
     */
    public abstract void addOutputListener(String terminal, OutputListener listener) throws JSONException;
    
    /** 
     * Where suitable this method will cause the module to be run.
     * <p>
     * There are two ways to trigger a module to run.
     * <p>
     * Modules that do not depend on any upstream modules will begin execution on this command.
     *    Typically these are Input modules and any workflows with no input prosts
     * <p>
     * Modules that depend on upstream modules should ignore this command.
     *    Instead these modules will execute upon receiving the last required input.
     *    As the execution of these modules is based on an OutputReady event, 
     *       it does not matter if a downstream module receives this call before the module providing input.
     * <p>
     * Modules that depend on upstream modules, 
     *     but that do not have enough input terminals connected, will simply not run.
     *     Their values and the values of any of their downstream modules will remain unchanged.
     *     No error is thrown.
     * <p>
     * When a module is run it must pass any output to any module listening on any of its output terminals.
     * 
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    public abstract void run(StringBuilder buffer) throws WireItRunException;
 
    public JSONObject getJsonObject() throws JSONException{
        JSONObject me = new JSONObject();
        me.put("name", name);
        me.put("config", config);
        
        JSONObject value = new JSONObject(values);
        me.put("value", value);
        me.put("config", config);
        return me;
     
     }

}
