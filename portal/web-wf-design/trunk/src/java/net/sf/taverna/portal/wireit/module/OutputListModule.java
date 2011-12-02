package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.utils.ListUtils;
import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * Module to receive the output from a depth 1 Upstream Modules and return these to wireIt.
 * <p>
 * This module is only expected to save any inputs which iut does in a single String 
 *   with the values tokenized with a newline.
 *   This format is used as it is the one expcted by WireIt's textfield.
 * <p>
 * WARNING: This is a prototype so does not yet handle all possible input types. Please Exstend accordingly.
 * @author Christian
 */
public class OutputListModule extends OutputModule implements OutputListener {
        
    /**
     * Constructor for passing the json to the super class.
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public OutputListModule (JSONObject json) throws JSONException{
        super(json);
    }
    
    /**
     * Revieves output which could be a list or even list of lists and saves A single long String.
     * <p>
     * The output format of a Singkle String with the values split by newlines #
     *    is because that is what WireIt's textField uses.
     * <p>
     * Lists of Lists are flattend.
     * <p>
     * WARNING: This is a prototype so does not yet handle all possible input types. Please Exstend accordingly.
     * @param outputBuilder Logging buffer. 
     * @throws WireItRunException An unexpected Object Type. Please extend the method,
     */
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException {
        //ystem.out.println("InnerListLisener.outputReady");
        //ystem.out.println(output);
        //ystem.out.println(output.getClass());
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
