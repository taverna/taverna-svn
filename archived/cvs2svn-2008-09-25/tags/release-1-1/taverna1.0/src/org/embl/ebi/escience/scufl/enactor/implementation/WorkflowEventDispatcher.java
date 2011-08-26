/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.event.*;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import java.util.*;

/**
 * The dispatcher class for workflow events, create one of
 * these per enactor instance and use it to push workflow
 * life cycle events out to interested parties.
 * @author Tom Oinn
 */
public class WorkflowEventDispatcher {
    
    private List eventQueue = new ArrayList();
    private List listeners = new ArrayList();
    private Thread notificationThread;
    
    public static WorkflowEventDispatcher DISPATCHER = new WorkflowEventDispatcher(true);

    /**
     * Create a new workflow event dispatcher. If the boolean
     * loadFromSPI is true then this will scan for implementations
     * of the WorkflowEventListener interface using the commons
     * discovery package, instantiate any found and add them
     * as listeners to this dispatcher. If false then this scan
     * is not performed and an empty dispatcher is created.
     */
    public WorkflowEventDispatcher(boolean loadFromSPI) {
	if (loadFromSPI) {
	    SPInterface spiIF = new SPInterface(WorkflowEventListener.class);
	    ClassLoaders loaders = new ClassLoaders();
	    loaders.put(Thread.currentThread().getContextClassLoader());
	    Enumeration spe = Service.providers(spiIF, loaders);
	    while (spe.hasMoreElements()) {
		WorkflowEventListener wel = (WorkflowEventListener)spe.nextElement();
		addListener(wel);
	    }	    
	}
	this.notificationThread = new NotifyThread();
    }
    
    public void fireWorkflowCreated(WorkflowCreationEvent e) {
	addEventToQueue(e);
    }
    
    public void fireWorkflowFailed(WorkflowFailureEvent e) {
	addEventToQueue(e);
    }
    
    public void fireWorkflowCompleted(WorkflowCompletionEvent e) {
	addEventToQueue(e);
    }

    public void fireProcessCompleted(ProcessCompletionEvent e) {
	addEventToQueue(e);
    }

    public void fireUserChangedData(UserChangedDataEvent e) {
	addEventToQueue(e);
    }
		

    public void fireIterationCompleted(IterationCompletionEvent e) {
	addEventToQueue(e);
    }
    
    public void fireProcessFailed(ProcessFailureEvent e) {
	addEventToQueue(e);
    }

    public void fireCollectionConstructed(CollectionConstructionEvent e) {
	addEventToQueue(e);
    }

    void fireEvents() {
	// Return if nothing to do
	if (hasPendingEvents() == false) {
	    return;
	}
	synchronized (this.listeners) {
	    // Get the list of events to send
	    WorkflowInstanceEvent[] events = getPendingEvents();
	    // Iterate over the listeners
	    for (Iterator i = listeners.iterator(); i.hasNext();) {
		WorkflowEventListener listener = (WorkflowEventListener)i.next();
		for (int j = 0; j < events.length; j++) {
		    try {
			sendAnEvent(listener, events[j]);
		    }
		    catch (Exception e) {
			// Silently ignore, was probably an exception in a plugin
		    }
		}
	    }
	}
    }

    public void addListener(WorkflowEventListener listener) {
	synchronized (this.listeners) {
	    if (listeners.contains(listener) == false) {
		listeners.add(listener);
	    }
	}
    }

    public void removeListener(WorkflowEventListener listener) {
	synchronized (this.listeners) {
	    listeners.remove(listener);
	}
    }

    private void sendAnEvent(WorkflowEventListener l, WorkflowInstanceEvent e) {
	if (e instanceof WorkflowCreationEvent) {
	    l.workflowCreated((WorkflowCreationEvent)e);
	    return;
	}
	else if (e instanceof WorkflowFailureEvent) {
	    l.workflowFailed((WorkflowFailureEvent)e);
	    return;
	}
	else if (e instanceof WorkflowCompletionEvent) {
	    l.workflowCompleted((WorkflowCompletionEvent)e);
	    return;
	}
	else if (e instanceof ProcessCompletionEvent) {
	    l.processCompleted((ProcessCompletionEvent)e);
	    return;
	}
	else if (e instanceof IterationCompletionEvent) {
	    l.processCompletedWithIteration((IterationCompletionEvent)e);
	    return;
	}
	else if (e instanceof ProcessFailureEvent) {
	    l.processFailed((ProcessFailureEvent)e);
	    return;
	}
	else if (e instanceof CollectionConstructionEvent) {
	    l.collectionConstructed((CollectionConstructionEvent)e);
	    return;
	}
	else if (e instanceof UserChangedDataEvent) {
	    l.dataChanged((UserChangedDataEvent)e);
	    return;
	}
    }

    private void addEventToQueue(WorkflowInstanceEvent e) {
	synchronized (this.eventQueue) {
	    this.eventQueue.add(e);
	    notificationThread.interrupt();
	}
    }
    
    private WorkflowInstanceEvent[] getPendingEvents() {
	synchronized (this.eventQueue) {
	    WorkflowInstanceEvent[] events = (WorkflowInstanceEvent[])this.eventQueue.toArray(new WorkflowInstanceEvent[0]);
	    this.eventQueue.clear();
	    return events;
	}
    }
    
    private boolean hasPendingEvents() {
	synchronized (this.eventQueue) {
	    return !this.eventQueue.isEmpty();
	}
    }

    class NotifyThread extends Thread {
	protected NotifyThread() {
	    super();
	    setDaemon(true);
	    start();
	}
	public void run() {
	    while (true) {
		WorkflowEventDispatcher.this.fireEvents();
		try {
		    // Sleep for ten seconds or until we're kicked
		    // by the addition of a new event
		    Thread.sleep(10000);
		}
		catch (InterruptedException ie) {
		    //
		}
	    }
	}
    }

}
