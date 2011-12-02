package net.sf.taverna.portal.wireit.module;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import net.sf.taverna.portal.wireit.event.OutputListener;

/**
 * This module for Comment was a Testing module. Funstionality may be INCORRECT!
 * 
 * Currently it overwrites the comment with a quick run message.
 * 
 * @author Christian
 */
public class CommentModule extends Module{

    /**
     * Constructor for passing the json to the super class.
     * @param json JSON representation of the modules.
     * @throws JSONException Thrown if the json is not in the expected format.
     */
    public CommentModule (JSONObject json) throws JSONException{
        super(json);
    }

    /**
     * Currently it overwrites the comment with a quick run message.
     * This is great for testing but may very well not be correct behaviour!
     * 
     * @param outputBuilder Logging buffer.
     * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
     */
    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        Date now = new Date();
        values.put("comment", "Ran successfully at " + now);
    }

    /**
     * Should never be called as comments have no outputs
     * 
     * @param terminal
     * @return
     * @throws JSONException Always thrown as comments have no outputs.
     */
    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        throw new JSONException("CommentModule has no Outputs");
    }

    /**
     * Should never ne called as comments have no inputs.
     * 
     * @param terminal
     * @param listener
     * @throws JSONException Always thrown as comments have no inputs.
     */
    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        throw new JSONException("CommentModule has no Inputs");
    }
    
}
