package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.utils.DelimiterURI;
import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.net.URISyntaxException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Input module for a depth 1 port where both a url and a delimiter are required.
 * <p>
 * The only role of this module is to pass the values received from WireIt to the downstream modules.
 * @author Christian
 */
public class InputDelimiterURIModule extends InputModule{
        
    /**
     * Constructor for passing the json to the super class.
     * 
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public InputDelimiterURIModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    @Override
    /** 
     * This method will cause the module to be run.
     * <p>
     * It simply formats and pass on the "url" and "delimiter" to any module listening on any of its output terminal.
     * 
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        System.out.println(values);
        Object uri = values.get("url");
        Object delimiter = values.get("delimiter");
        try {
            DelimiterURI value = new DelimiterURI(uri.toString(), delimiter.toString());
            output.fireOutputReady(value, outputBuilder);
        } catch (URISyntaxException ex) {
            throw new WireItRunException("Ilegal URI: " + uri, ex);
        }
    }

}
