/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.module;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.event.OutputListener;

/**
 *
 * @author Christian
 */
public class CommentModule extends Module{

    public CommentModule (JSONObject json) throws JSONException{
        super(json);
    }

    @Override
    public void run(StringBuilder outputBuilder) throws WireItRunException {
        Date now = new Date();
        values.put("comment", "Ran successfully at " + now);
    }

    @Override
    public OutputListener getOutputListener(String terminal) throws JSONException {
        throw new JSONException("CommentModule has no Outputs");
    }

    @Override
    public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
        throw new JSONException("CommentModule has no Inputs");
    }
    
}
