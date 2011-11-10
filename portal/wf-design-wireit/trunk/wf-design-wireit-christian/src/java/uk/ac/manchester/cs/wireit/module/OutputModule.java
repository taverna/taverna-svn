package uk.ac.manchester.cs.wireit.module;

import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public class OutputModule extends Module{
       
    final String PORT_NAME = "input";
    
    public OutputModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        //Do nothing reacts to push not run()
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals(PORT_NAME)){
            return new InnerLisener();
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected input");
        }
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        throw new JSONException("Module OutputPort has no output ports");
    }

    private class InnerLisener implements OutputListener{

        @Override
        public void outputReady(Object output, StringBuilder outputBuilder) {
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
}
