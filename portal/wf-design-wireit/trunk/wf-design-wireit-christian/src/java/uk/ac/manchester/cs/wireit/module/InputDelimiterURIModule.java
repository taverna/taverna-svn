package uk.ac.manchester.cs.wireit.module;

import java.net.URI;
import java.net.URISyntaxException;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputFirer;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public class InputDelimiterURIModule extends Module{
        
    private OutputFirer output;
    
    private final String PORT_NAME = "output";

    public InputDelimiterURIModule (JSONObject json) throws JSONException{
        super(json);
        output = new OutputFirer();
    }
    
    @Override
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

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        throw new JSONException("InputModule has no Inputs");
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (terminal.equals(PORT_NAME)){
            output.addOutputListener(listener);
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected output");
        }
    }

}
