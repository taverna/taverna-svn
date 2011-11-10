package uk.ac.manchester.cs.wireit.module;

import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public abstract class Module {
    
    String name;
    JSONObject config;
    HashMap <String, Object> values;
    
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
    
    public abstract OutputListener getOutputListener(String terminal) throws JSONException;

    public abstract void addOutputListener(String terminal, OutputListener listener) throws JSONException;
    
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
