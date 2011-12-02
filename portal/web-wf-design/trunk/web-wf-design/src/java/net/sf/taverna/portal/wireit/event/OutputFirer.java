package net.sf.taverna.portal.wireit.event;

import java.util.ArrayList;
import net.sf.taverna.portal.wireit.exception.WireItRunException;

/**
 * Support class which handles the firing of output ready to zero or more connected OutputListeners.
 * <p>
 * By using this class the individual modules do not need to worry about which output ports are connected and 
 * to how many modules they are connected.
 * <p>
 * Terminals not wired will have no associated Listeners so the fireOutputReady method simply return job done.
 * <p>
 * For Terminals with one connected module fireOutputReady just passes the data to that module.
 * <p>
 * For Terminals with multiple connected modules, each module will be passed the object in turn. 
 * This even allows the same output terminal to be connected to various input terminals on the same downstream module.
 * 
 * @author Christian
 */
public class OutputFirer {
    
    /** 
     * Store of the associated Listeners 
     */
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
    
    /**
     * Passes the Object on to any downstream modules,
     * <p>
     * This could cuase the module to execute.
     * Any log information is written to the outputBuilder.
     * @param output Information being passed from one module to another.
     * @param outputBuilder Logging buffer. 
     * @throws WireItRunException Something has gone wrong. This could be caused by exectution 
     *    or even one of the downstream modules.
     */
    public void fireOutputReady(Object output, StringBuilder outputBuilder) throws WireItRunException {
        for (OutputListener listener: listeners){
            listener.outputReady(output, outputBuilder);
        }
    }   
    
 
}
