/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowDestroyedEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowInstanceEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;

/**
 * The dispatcher class for workflow events, create one of these per enactor
 * instance and use it to push workflow life cycle events out to interested
 * parties.
 * 
 * @author Tom Oinn
 */
public class WorkflowEventDispatcher {
	
	private static List<WorkflowEventListener> spiListeners=new ArrayList<WorkflowEventListener>();

    private static final Logger logger = Logger
            .getLogger(WorkflowEventDispatcher.class);

	private List<WorkflowInstanceEvent> eventQueue = new ArrayList<WorkflowInstanceEvent>();
	
	/**
	 * List of listeners. This must be in order of calls to 
	 * addListener(), so that WorkflowInstanceImpl.destroy() works
	 * properly.
	 * 
	 */
	private List<WorkflowEventListener> listeners = new ArrayList<WorkflowEventListener>();

	private Thread notificationThread;

	public static WorkflowEventDispatcher DISPATCHER = new WorkflowEventDispatcher(
			true);

	/**
	 * Create a new workflow event dispatcher. If the boolean loadFromSPI is
	 * true then this will scan for implementations of the WorkflowEventListener
	 * interface using the commons discovery package, instantiate any found and
	 * add them as listeners to this dispatcher. If false then this scan is not
	 * performed and an empty dispatcher is created.
	 */
	public WorkflowEventDispatcher(boolean loadFromSPI) {
		if (loadFromSPI) {
			addEventListenersFromSPI();
			WorkflowEventListenerRegistry.getInstance().addRegistryListener(new RegistryListener() {
				public void spiRegistryUpdated(SpiRegistry registry) {
					addEventListenersFromSPI();					
				}				
			});			
		}
		// FIXME: No way to kill notification thread
		this.notificationThread = new NotifyThread();
	}

	private void addEventListenersFromSPI() {
		List<WorkflowEventListener> listenersFromSPI = WorkflowEventListenerRegistry
				.getInstance().getWorkflowEventListeners();
		
		//first add any new ones
		for (WorkflowEventListener listener : listenersFromSPI) {			
			addListener(listener);			
			if (!spiListeners.contains(listener)) spiListeners.add(listener);
		}
		
		//then remove any listeners that were previously added but no longer exist - for example, the LogBook plugin has been disabled.		
		List<WorkflowEventListener> forRemoval = new ArrayList<WorkflowEventListener>();
		for (WorkflowEventListener oldListener : spiListeners) {
			if (!listenersFromSPI.contains(oldListener)) {
				logger.info("Removing workflow listener:"+oldListener);
				forRemoval.add(oldListener);
				removeListener(oldListener);
			}
		}
		spiListeners.removeAll(forRemoval);
	}
	
	// Use fireEvent(WorkflowEventListener) instead of these
	@Deprecated
	public void fireNestedWorkflowCreated(NestedWorkflowCreationEvent e) {
		addEventToQueue(e);
	}
	@Deprecated	
	public void fireNestedWorkflowFailed(NestedWorkflowFailureEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireNestedWorkflowCompleted(NestedWorkflowCompletionEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireWorkflowCreated(WorkflowCreationEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireWorkflowFailed(WorkflowFailureEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireWorkflowCompleted(WorkflowCompletionEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireProcessCompleted(ProcessCompletionEvent e) {		
		addEventToQueue(e);
	}
	@Deprecated
	public void fireUserChangedData(UserChangedDataEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireIterationCompleted(IterationCompletionEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireProcessFailed(ProcessFailureEvent e) {
		addEventToQueue(e);
	}
	@Deprecated
	public void fireCollectionConstructed(CollectionConstructionEvent e) {
		addEventToQueue(e);
	}
	
	/**
	 * Fire an event.
	 * <p>
	 * Use this function instead of the numerous 
	 * <code>fireCollectionConstructed()</code> etc.
	 * 
	 * @param event The event to fire
	 */
	public void fireEvent(WorkflowInstanceEvent event) {
		addEventToQueue(event);
	}

	public void addListener(WorkflowEventListener listener) {
		synchronized (listeners) {
			if (! listeners.contains(listener)) {
				logger.info("Adding Workflow Listener:"+listener);
				listeners.add(listener);
			}
		}
	}

	public void removeListener(WorkflowEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	void fireEvents() {
		WorkflowInstanceEvent[] events = getPendingEvents();
		for (WorkflowInstanceEvent event : events) {
			// Send the event to each listener in order
			synchronized (listeners) {
				// Iterate over a copy to avoid ConcurrentModificationException in case
				// one of our listeners do addListener() or removeListener().
				for (WorkflowEventListener listener : listeners.toArray(new WorkflowEventListener[0])) {	
					try {
						sendAnEvent(listener, event);
					} catch (Exception e) {
						logger.error("Could not send event to " + listener, e);
					}
				}
			}
		}
	}

	private void sendAnEvent(WorkflowEventListener l, WorkflowInstanceEvent e) {
		if (e instanceof WorkflowCreationEvent) {
			l.workflowCreated((WorkflowCreationEvent) e);
		} else if (e instanceof WorkflowFailureEvent) {
			l.workflowFailed((WorkflowFailureEvent) e);
		} else if (e instanceof WorkflowCompletionEvent) {
			l.workflowCompleted((WorkflowCompletionEvent) e);
		} else if (e instanceof ProcessCompletionEvent) {
			l.processCompleted((ProcessCompletionEvent) e);
		} else if (e instanceof IterationCompletionEvent) {
			l.processCompletedWithIteration((IterationCompletionEvent) e);
		} else if (e instanceof NestedWorkflowCompletionEvent) {
			l.nestedWorkflowCompleted((NestedWorkflowCompletionEvent)e);
		} else if (e instanceof NestedWorkflowFailureEvent) {
			l.nestedWorkflowFailed((NestedWorkflowFailureEvent)e);
		} else if (e instanceof NestedWorkflowCreationEvent) {
			l.nestedWorkflowCreated((NestedWorkflowCreationEvent)e);
		} else if (e instanceof ProcessFailureEvent) {
			l.processFailed((ProcessFailureEvent) e);
		} else if (e instanceof CollectionConstructionEvent) {
			l.collectionConstructed((CollectionConstructionEvent) e);
		} else if (e instanceof UserChangedDataEvent) {
			l.dataChanged((UserChangedDataEvent) e);
		} else if (e instanceof WorkflowToBeDestroyedEvent) {
			l.workflowToBeDestroyed((WorkflowToBeDestroyedEvent) e);
		} else if (e instanceof WorkflowDestroyedEvent) {
			l.workflowDestroyed((WorkflowDestroyedEvent) e);
		}
	}

	private void addEventToQueue(WorkflowInstanceEvent e) {
		synchronized (eventQueue) {
			eventQueue.add(e);
            synchronized (notificationThread) {
                notificationThread.notify();
            }
		}
	}

	private WorkflowInstanceEvent[] getPendingEvents() {
		synchronized (eventQueue) {
			WorkflowInstanceEvent[] events = eventQueue.toArray(new WorkflowInstanceEvent[0]);
			eventQueue.clear();
			return events;
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
				fireEvents();
				try {
					// wait for ten seconds until we're kicked
					// by the addition of a new event
                    synchronized (this) {                        
                        this.wait(10000);
                    }
				} catch (InterruptedException ie) {
					// We got notified, some new events coming up in the loop
				}
			}
		}
	}

}
