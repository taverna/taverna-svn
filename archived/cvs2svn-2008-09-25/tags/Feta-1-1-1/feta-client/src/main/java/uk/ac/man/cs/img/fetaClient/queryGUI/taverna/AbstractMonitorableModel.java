/*
 * AbstractMonitorableModel.java
 *
 * Created on April 28, 2005, 1:28 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * 
 * @author alperp
 */

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public abstract class AbstractMonitorableModel {
	private EventListenerList changeListeners = new EventListenerList();

	/** Creates a new instance of AbstractMonitorableModel */
	public AbstractMonitorableModel() {
	}

	public void addChangeListener(ChangeListener x) {
		changeListeners.add(ChangeListener.class, x);

		// bring it up to date with current state
		x.stateChanged(new ChangeEvent(this));
	}

	public void addChangeListenerQuitely(ChangeListener x) {
		changeListeners.add(ChangeListener.class, x);

		// do not bring it up to date with current state
		// x.stateChanged(new ChangeEvent(this));
	}

	public void removeChangeListener(ChangeListener x) {
		changeListeners.remove(ChangeListener.class, x);
	}

	public void fireChange() {

		// System.out.println("Debug in Fire Change");
		// Create the event:
		ChangeEvent c = new ChangeEvent(this);
		// Get the listener list
		Object[] listeners = changeListeners.getListenerList();
		// Process the listeners last to first
		// List is in pairs, Class and instance
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				ChangeListener cl = (ChangeListener) listeners[i + 1];
				cl.stateChanged(c);
			}
		}
	}
}
