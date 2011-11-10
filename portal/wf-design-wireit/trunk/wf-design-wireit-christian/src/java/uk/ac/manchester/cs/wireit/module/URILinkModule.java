/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class URILinkModule extends Module{

    private final String OUTPUT_PORT_NAME = "output";
    private final String INPUT_PORT_NAME = "input";
    private final String VALUE_SAVE_NAME = "uri";
            
    private OutputFirer outputFirer;
    private boolean expectingInput;

    public URILinkModule (JSONObject json) throws JSONException{
        super(json);
        outputFirer = new OutputFirer();
        expectingInput = false;
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals(INPUT_PORT_NAME)){
            expectingInput = true;
            return new InnerLisener();
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
 
    private class InnerLisener implements OutputListener{

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

}
