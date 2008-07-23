package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Adds an output port to an activity.
 * 
 * @author David Withers
 */
public class AddActivityOutputPortEdit extends AbstractActivityEdit {

	private OutputPort activityOutputPort;

	public AddActivityOutputPortEdit(Activity<?> activity, OutputPort activityInputPort) {
		super(activity);
		this.activityOutputPort = activityInputPort;
	}

	@Override
	protected void doEditAction(AbstractActivity<?> activity) throws EditException {
		activity.getOutputPorts().add(activityOutputPort);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getOutputPorts().remove(activityOutputPort);
	}

}
