package uk.ac.manchester.cs.wireit.event;

import java.util.ArrayList;
import uk.ac.manchester.cs.wireit.module.WireItRunException;

public class OutputFirer {
    
    private ArrayList<OutputListener> listeners = new ArrayList<OutputListener>();

    /**
     * Adds an <code>OutputListener</code> to thiis class.
     * 
     * If the <code>OutputListener</code> has already been registered it is ignored.
     * 
     * @param l the listener to be added
     */

    public void addOutputListener(OutputListener l) {
        if (!listeners.contains(l)){
            listeners.add(l);
        }
    }
    
    /**
     * Removes an <code>OutputListener</code> from this class;
     * 
     * If the <code>OutputListener</code> is not present this method does nothing.
     * 
     * @param l the listener to be removed
     */
    public void removeOutoutListener(OutputListener l) {
        listeners.remove(l);
    }
    
    public void fireOutputReady(Object output, StringBuilder outputBuilder) throws WireItRunException {
        for (OutputListener listener: listeners){
            listener.outputReady(output, outputBuilder);
        }
    }   
    
 
}
