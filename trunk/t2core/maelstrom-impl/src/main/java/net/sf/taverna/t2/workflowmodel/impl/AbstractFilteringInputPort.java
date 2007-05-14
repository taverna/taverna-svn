package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Iterator;

import net.sf.taverna.t2.cloudone.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.FilteringInputPort;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;

/**
 * Abstract superclass for filtering input ports, extend and implement the
 * pushXXX methods to configure behaviour
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractFilteringInputPort extends AbstractPort implements
		FilteringInputPort {

	protected AbstractFilteringInputPort(String name, int depth) {
		super(name, depth);
		this.filterDepth = depth;
	}

	private int filterDepth;

	public void receiveEvent(Event e) {
		if (e instanceof WorkflowDataToken) {
			receiveToken((WorkflowDataToken) e);
		} else {
			throw new WorkflowStructureException(
					"Filtering input port only accepts WorkflowDataToken");
		}
	}

	// FIXME - this should really be per-process or at least be possible to
	// reset.
	private int oid = -1;

	// FIXME - this should really be per-process or at least be possible to
	// reset.
	public int getObservedDepth() {
		return this.oid;
	}

	public void pushToken(WorkflowDataToken dt, String owningProcess, int desiredDepth) {
		if (dt.getData().getDepth() == desiredDepth) {
			//System.out.println("** Job : "+dt.getData());
			pushData(getName(), owningProcess, dt.getIndex(), dt.getData());
		} else {
			DataManager dManager = ContextManager.getDataManager(owningProcess);
			Iterator<ContextualizedIdentifier> children = dManager.traverse(
					dt.getData(), dt.getData().getDepth()-1);
			while (children.hasNext()) {
				ContextualizedIdentifier ci = children.next();
				int[] newIndex = new int[dt.getIndex().length
						+ ci.getIndex().length];
				int i = 0;
				for (int indx : dt.getIndex()) {
					newIndex[i++] = indx;
				}
				for (int indx : ci.getIndex()) {
					newIndex[i++] = indx;
				}
				pushToken(new WorkflowDataToken(owningProcess, newIndex, ci.getDataRef()), owningProcess, desiredDepth);
			}
			//System.out.println("** Completion : "+dt.getData());
			pushCompletion(getName(), owningProcess, dt.getIndex());
		}
	}

	public void receiveToken(WorkflowDataToken token) {
		this.oid = token.getIndex().length + token.getData().getDepth();
		String newOwner = transformOwningProcess(token.getOwningProcess());
		if (filterDepth == -1) {
			throw new WorkflowStructureException(
					"Input depth filter not configured on input port, failing");
		} else {
			int tokenDepth = token.getData().getDepth();
			if (tokenDepth == filterDepth) {
				if (filterDepth == getDepth()) {
					// Pass event straight through, the filter depth is the same
					// as the desired input port depth
					pushData(getName(), newOwner, token.getIndex(), token
							.getData());
				} else {
					pushToken(token, newOwner, getDepth());
					/**
					// Shred the input identifier into the appropriate port
					// depth and send the events through, pushing a
					// completion event at the end.
					DataManager dManager = ContextManager
							.getDataManager(newOwner);
					Iterator<ContextualizedIdentifier> children = dManager
							.traverse(token.getData(), getDepth());
					while (children.hasNext()) {
						ContextualizedIdentifier ci = children.next();
						int[] newIndex = new int[token.getIndex().length
								+ ci.getIndex().length];
						int i = 0;
						for (int indx : token.getIndex()) {
							newIndex[i++] = indx;
						}
						for (int indx : ci.getIndex()) {
							newIndex[i++] = indx;
						}
						pushData(getName(), newOwner, newIndex, ci.getDataRef());
					}
					pushCompletion(getName(), newOwner, token.getIndex());
					*/

				}
			} else if (tokenDepth > filterDepth) {
				// Convert to a completion event and push into the iteration
				// strategy
				pushCompletion(getName(), newOwner, token.getIndex());
			} else if (tokenDepth < filterDepth) {
				// Normally we can ignore these, but there is a special case
				// where token depth is less than filter depth and there is no
				// index array. In this case we can't throw the token away as
				// there will never be an enclosing one so we have to use the
				// data manager to register a new single element collection and
				// recurse.
				if (token.getIndex().length == 0) {
					DataManager dManager = ContextManager
							.getDataManager(newOwner);
					EntityIdentifier ref = token.getData();
					int currentDepth = tokenDepth;
					while (currentDepth < filterDepth) {
						ref = dManager
								.registerList(new EntityIdentifier[] { ref });
						currentDepth++;
					}
					pushData(getName(), newOwner, new int[0], ref);
				}
			}
		}
	}

	public void setFilterDepth(int filterDepth) {
		this.filterDepth = filterDepth;
	}

	/**
	 * Action to take when the filter pushes a completion event out
	 * 
	 * @param portName
	 * @param owningProcess
	 * @param index
	 */
	protected abstract void pushCompletion(String portName,
			String owningProcess, int[] index);

	/**
	 * Action to take when a data event is created by the filter
	 * 
	 * @param portName
	 * @param owningProcess
	 * @param index
	 * @param data
	 */
	protected abstract void pushData(String portName, String owningProcess,
			int[] index, EntityIdentifier data);

	/**
	 * Override this to transform owning process identifiers as they pass
	 * through the filter, by default this is the identity transformation
	 * 
	 * @param oldOwner
	 * @return
	 */
	protected String transformOwningProcess(String oldOwner) {
		return oldOwner;
	}

}
