package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

/**
 * Adds an activity input port to an activity.
 * 
 * @author David Withers
 */
public class AddActivityInputPortEdit extends AbstractActivityEdit {

	private ActivityInputPort activityInputPort;

	public AddActivityInputPortEdit(Activity<?> activity, ActivityInputPort activityInputPort) {
		super(activity);
		this.activityInputPort = activityInputPort;
	}

	@Override
	protected void doEditAction(AbstractActivity<?> activity) throws EditException {
		activity.getInputPorts().add(activityInputPort);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getInputPorts().remove(activityInputPort);
	}

}
