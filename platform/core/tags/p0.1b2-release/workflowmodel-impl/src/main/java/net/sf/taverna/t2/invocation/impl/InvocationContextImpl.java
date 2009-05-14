package net.sf.taverna.t2.invocation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.InvocationContextException;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Simple implementation of InvocationContext which stores all context resources
 * in an in-memory set.
 * 
 * @author Tom Oinn
 * 
 */
public class InvocationContextImpl implements InvocationContext {

	private Set<Object> items = new HashSet<Object>();
	private ReferenceService referenceService = null;
	private Set<ProcessIdentifier> cancelledProcesses = new HashSet<ProcessIdentifier>();
	private boolean paused = false;
	private List<Thread> pausedThreads = new ArrayList<Thread>();
	private Map<ProcessIdentifier, Dataflow> activeFlows = new HashMap<ProcessIdentifier, Dataflow>();

	/**
	 * Construct a new (empty) invocation context
	 */
	public InvocationContextImpl() {
		//
	}

	public synchronized final void addEntity(Object entity) {
		items.add(entity);
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public synchronized final <T> List<? extends T> getEntities(
			Class<T> entityClass) {
		List<T> result = new ArrayList<T>();
		// Add all matching items in the item set
		filterAndAdd(items, result, entityClass);
		// Add the reference service special case if appropriate
		if (referenceService != null) {
			if (entityClass.isAssignableFrom(referenceService.getClass())) {
				result.add(entityClass.cast(referenceService));
			}
		}
		return result;
	}

	public synchronized final <T> T getEntity(Class<T> entityClass) {
		List<? extends T> results = getEntities(entityClass);
		if (results.isEmpty()) {
			throw new InvocationContextException(
					"No entities in invocation context assignable to "
							+ entityClass.getCanonicalName());
		} else if (results.size() > 1) {
			throw new InvocationContextException(
					"Multiple entities in invocation context assignable to "
							+ entityClass.getCanonicalName());
		}
		return results.get(0);
	}

	private synchronized <T extends Object> void filterAndAdd(
			Iterable<?> source, List<T> target, Class<T> type) {
		for (Object o : source) {
			if (type.isAssignableFrom(o.getClass())) {
				T targetObject = type.cast(o);
				target.add(targetObject);
			}
		}
	}

	public final ReferenceService getReferenceService() {
		return this.referenceService;
	}

	public boolean isActive(ProcessIdentifier owningProcess) {
		if (paused) {
			synchronized (pausedThreads) {
				pausedThreads.add(Thread.currentThread());
			}
			while (paused) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					//
				}
			}
		}
		for (ProcessIdentifier cancelledProcess : cancelledProcesses) {
			if (owningProcess.toString()
					.startsWith(cancelledProcess.toString())) {
				return false;
			}
		}
		return true;
	}

	public void setProcessInactive(ProcessIdentifier owningProcess) {
		cancelledProcesses.add(owningProcess);
	}

	public boolean isPaused() {
		synchronized (pausedThreads) {
			return this.paused;
		}
	}

	public void setPaused(boolean paused) {
		synchronized (pausedThreads) {
			if (paused != this.paused) {
				this.paused = paused;
				synchronized (activeFlows) {
					for (ProcessIdentifier flowId : activeFlows.keySet()) {
						Dataflow flow = activeFlows.get(flowId);
						WorkflowInstanceStatus status = flow
								.getInstanceStatus(flowId);
						if (paused
								&& status
										.equals(WorkflowInstanceStatus.RUNNING)) {
							flow.setWorkflowInstanceStatus(flowId,
									WorkflowInstanceStatus.PAUSED);
						}
						if (!paused
								&& status.equals(WorkflowInstanceStatus.PAUSED)) {
							flow.setWorkflowInstanceStatus(flowId,
									WorkflowInstanceStatus.RUNNING);
						}
					}
				}
			}
		}
		if (!paused) {
			for (Thread t : pausedThreads) {
				t.interrupt();
			}
			pausedThreads.clear();
		}
	}

	public void registerDataflow(Dataflow dataflow,
			ProcessIdentifier dataflowProcessId) {
		synchronized (activeFlows) {
			activeFlows.put(dataflowProcessId, dataflow);
		}
	}

	public void unregisterDataflow(ProcessIdentifier dataflowProcessId) {
		synchronized (activeFlows) {
			activeFlows.remove(dataflowProcessId);
		}
	}

}
