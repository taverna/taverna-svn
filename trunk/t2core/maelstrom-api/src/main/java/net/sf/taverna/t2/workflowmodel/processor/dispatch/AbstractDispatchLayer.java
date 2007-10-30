package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * Convenience abstract implementation of DispatchLayer, all actions are set to
 * DispatchLayerAction.PASSTHROUGH other than JOBQUEUE which is set to FORBIDDEN
 * and all events are handed without modification to the layer directly above or
 * below as appropriate. Default state of the behaviour metadata is therefore as
 * follows : <table>
 * <tr>
 * <th>DispatchMessageType</th>
 * <th>DispatchLayerAction</th>
 * <th>canProduce</th>
 * </tr>
 * <tr>
 * <td>ERROR</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>JOB</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>JOBQUEUE</td>
 * <td>FORBIDDEN</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>RESULT</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr> *
 * <tr>
 * <td>RESULTCOMPLETION</td>
 * <td>PASSTHROUGH</td>
 * <td>false</td>
 * </tr>
 * </table>
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractDispatchLayer<ConfigurationType> implements
		DispatchLayer<ConfigurationType> {

	public void setDispatchStack(DispatchStack parentStack) {

		this.dispatchStack = parentStack;

	}

	protected Map<DispatchMessageType, DispatchLayerAction> messageActions = new HashMap<DispatchMessageType, DispatchLayerAction>();

	protected Map<DispatchMessageType, Boolean> producesMessage = new HashMap<DispatchMessageType, Boolean>();

	private DispatchStack dispatchStack;

	/**
	 * Throws WorkflowStructureException if the specified message type is set to
	 * FORBIDDEN in the messageActions map
	 * 
	 * @param type
	 */
	protected void checkValid(DispatchMessageType type) {
		if (messageActions.get(type).equals(DispatchLayerAction.FORBIDDEN)) {
			throw new WorkflowStructureException("Cannot handle message type "
					+ type + " in dispatch layer " + this.getClass().getName());
		}
	}

	/**
	 * Initialize all message actions to PASSTHROUGH other than JOBQUEUE which
	 * is set to FORBIDDEN and all message production booleans to false by
	 * default.
	 * 
	 */
	protected AbstractDispatchLayer() {
		for (DispatchMessageType mt : DispatchMessageType.values()) {
			messageActions.put(mt, DispatchLayerAction.PASSTHROUGH);
			producesMessage.put(mt, false);
		}
		messageActions.put(DispatchMessageType.JOBQUEUE,
				DispatchLayerAction.FORBIDDEN);
	}

	protected final DispatchLayer<?> getAbove() {
		return this.dispatchStack.layerAbove(this);
	}

	protected final DispatchLayer<?> getBelow() {
		return this.dispatchStack.layerBelow(this);
	}

	public final DispatchLayerAction getActionFor(
			DispatchMessageType messageType) {
		return messageActions.get(messageType);
	}

	public final Boolean canProduce(DispatchMessageType messageType) {
		return producesMessage.get(messageType);
	}

	public void receiveError(String owningProcess, int[] errorIndex,
			String errorMessage, Throwable detail) {
		checkValid(DispatchMessageType.ERROR);
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveError(owningProcess, errorIndex, errorMessage, detail);
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveJob(Job job, List<? extends Activity<?>> activities) {
		checkValid(DispatchMessageType.JOB);
		DispatchLayer<?> below = dispatchStack.layerBelow(this);
		if (below != null) {
			below.receiveJob(job, activities);
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveJobQueue(String owningProcess,
			BlockingQueue<Event> queue, List<? extends Activity<?>> activities) {
		checkValid(DispatchMessageType.JOBQUEUE);
		DispatchLayer below = dispatchStack.layerBelow(this);
		if (below != null) {
			below.receiveJobQueue(owningProcess, queue, activities);
		}

	}

	public void receiveResult(Job job) {
		checkValid(DispatchMessageType.RESULT);
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveResult(job);
		}
	}

	public void receiveResultCompletion(Completion completion) {
		checkValid(DispatchMessageType.RESULTCOMPLETION);
		DispatchLayer<?> above = dispatchStack.layerAbove(this);
		if (above != null) {
			above.receiveResultCompletion(completion);
		}

	}

	public void finishedWith(String owningProcess) {
		//
	}

}
