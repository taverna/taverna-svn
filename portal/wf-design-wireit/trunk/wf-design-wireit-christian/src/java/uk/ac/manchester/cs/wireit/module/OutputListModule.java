package uk.ac.manchester.cs.wireit.module;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public class OutputListModule extends OutputModule{
       
    public OutputListModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        if (terminal.equals(PORT_NAME)){
            return new InnerListLisener();
        } else {
            throw new JSONException("Unsupported port name " + terminal + " expected input");
        }
    }

    private class InnerListLisener implements OutputListener{

        @Override
        public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException {
            System.out.println("InnerListLisener.outputReady");
            System.out.println(output);
            System.out.println(output.getClass());
            String[] array;
            if (output instanceof String[]){
                array = (String[]) output;
            } else if (output instanceof ArrayList){
                array = ListUtils.toStringArray(output);
            } else {
                throw new WireItRunException("Unexpected output type " + output.getClass());
            }
            if (array.length == 0){
                values.put(PORT_NAME, "");
            } else {
                StringBuilder builder = new StringBuilder(array[0]);
                for (int i = 1; i < array.length; i++ ){
                    builder.append("\n");
                    builder.append(array[i]);
                }
                values.put(PORT_NAME, builder.toString());
            }       
        }
        
    }
}
