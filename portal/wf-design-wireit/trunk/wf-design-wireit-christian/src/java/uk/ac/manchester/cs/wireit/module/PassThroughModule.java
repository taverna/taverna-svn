package uk.ac.manchester.cs.wireit.module;

import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputFirer;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public class PassThroughModule extends Module{
        
    OutputFirer outputFirer;
    
    public PassThroughModule (JSONObject json) throws JSONException{
        super(json);
        outputFirer = new OutputFirer();
    }
    
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        //Do nothing reacts to push not run()
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals("input")){
            return new InnerLisener();
        } else {
            throw new JSONException("Unsupported port name: " + terminal);
        }
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        if (terminal.equals("output")){
            outputFirer.addOutputListener(listener);
        } else {
            throw new JSONException("Unsupported port name: " + terminal);
        }
    }

    private class InnerLisener implements OutputListener{

        @Override
        public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
            values.put("both", output);
            outputFirer.fireOutputReady(output, outputBuilder);
        }
    }
}
