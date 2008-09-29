package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

/**
 * Removes an activity input port from an activity.
 * 
 * @author David Withers
 */
public class RemoveActivityInputPortEdit extends AbstractActivityEdit {

	private ActivityInputPort activityInputPort;

	public RemoveActivityInputPortEdit(Activity<?> activity, ActivityInputPort activityInputPort) {
		super(activity);
		this.activityInputPort = activityInputPort;
	}

	@Override
	protected void doEditAction(AbstractActivity<?> activity) throws EditException {
		activity.getInputPorts().remove(activityInputPort);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getInputPorts().add(activityInputPort);
	}

}
