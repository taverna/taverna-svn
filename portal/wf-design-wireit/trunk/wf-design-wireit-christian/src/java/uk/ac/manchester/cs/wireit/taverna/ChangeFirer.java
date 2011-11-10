package uk.ac.manchester.cs.wireit.taverna;

import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Thin superclass which holds ChangeListeners and handles the firing of change events.
 * 
 * @author Christian
 */
public class ChangeFirer {
    
    private ChangeEvent changeEvent;
    
    private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

    /**
     * Adds a <code>ChangeListener</code> to thiis class.
     * 
     * If the ChangeListener has already been registered it is ignored.
     * 
     * @param l the listener to be added
     */
    public void addChangeListener(ChangeListener l) {
        if (!listeners.contains(l)){
            listeners.add(l);
        }
    }
    
    /**
     * Removes a ChangeListener from this class;
     * 
     * If the ChangeListener is not present this method does nothing.
     * 
     * @param l the listener to be removed
     */
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    void fireStateChanged() {
        if (changeEvent == null)
            changeEvent = new ChangeEvent(this);
        for (ChangeListener listener: listeners){
            listener.stateChanged(changeEvent);
        }
    }   
    
 
}
