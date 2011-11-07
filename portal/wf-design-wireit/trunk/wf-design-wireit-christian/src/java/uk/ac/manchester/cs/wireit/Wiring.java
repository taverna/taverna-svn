/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputListener;
import uk.ac.manchester.cs.wireit.module.*;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;

/**
 *
 * @author Christian
 */
public class Wiring {
    
    Module[] modules;
    JSONObject properties;
    JSONArray wires;
    
    public Wiring(JSONObject jsonInput, StringBuffer URL) throws JSONException, TavernaException, IOException{
        JSONArray jsonArray = jsonInput.getJSONArray("modules");
        modules = new Module[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++){
            Object json = jsonArray.get(i);
            if (json instanceof JSONObject){
                JSONObject jsonObject = (JSONObject)json;
                String name = jsonObject.getString("name");
                if (name.equals("Simple Input")){
                   modules[i] = new InputStringModule(jsonObject); 
                } else if (name.equals("List Input")){
                   modules[i] = new InputListModule(jsonObject); 
                } else if (name.equals("URL Input")){
                   modules[i] = new InputURIModule(jsonObject); 
                } else if (name.equals("URL To List Input")){
                   modules[i] = new InputDelimiterURIModule(jsonObject); 
                } else if (name.equals("Simple Output") || name.equals("URL Output")){
                   modules[i] = new OutputModule(jsonObject); 
                } else if (name.equals("List Output")){
                   modules[i] = new OutputListModule(jsonObject); 
                } else if (name.equals("PassThrough")){
                   modules[i] = new PassThroughModule(jsonObject); 
                } else if (name.equals("comment")){
                   modules[i] = new CommentModule(jsonObject); 
                } else if (jsonObject.has("config")){
                    JSONObject config = jsonObject.getJSONObject("config");
                    String xtype = config.optString("xtype");
                    if ("WireIt.TavernaWFContainer".equalsIgnoreCase(xtype)){
                       modules[i] = new TavernaModule(jsonObject, URL); 
                    } else {
                        throw new JSONException("Unexpected name " + name + " and xtype " + xtype + " in modules");
                    }
                } else {
                    throw new JSONException("Unexpected name " + name + "and no config in modules");
                }
            } else {
                throw new JSONException("Unexpected type " + json.getClass() + " in modules");
            }
        }
        properties = jsonInput.getJSONObject("properties");
        wires = jsonInput.getJSONArray("wires");
        for (int i = 0; i < wires.length(); i++){
            JSONObject wire = wires.optJSONObject(i);
            JSONObject tgt = wire.getJSONObject("tgt");
            int tgtNum = tgt.getInt("moduleId");
            Module target = modules[tgtNum];
            String terminal = tgt.getString("terminal");
            System.out.println(tgtNum + " " + terminal + " " + target);
            OutputListener outputListener = target.getOutputListener(terminal);
            JSONObject src = wire.getJSONObject("src");
            int srcNum = src.getInt("moduleId");
            Module source = modules[srcNum];
            terminal = src.getString("terminal");
            source.addOutputListener(terminal, outputListener);           
        }
    }
    
    public void run(StringBuilder outputBuilder) throws WireItRunException{
        for (int i = 0; i < modules.length; i++){
            modules[i].run(outputBuilder);
        }
    }
    
    public JSONObject getJsonObject() throws JSONException{
        JSONObject me = new JSONObject();
        me.put("wires", wires);
        me.put("properties", properties);
        for (int i = 0; i < modules.length; i++){
            me.append("modules", modules[i].getJsonObject());
        }       
        return me;
    }
}
