package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Removes an output port from an activity.
 * 
 * @author David Withers
 */
public class RemoveActivityOutputPortEdit extends AbstractActivityEdit {

	private OutputPort activityOutputPort;

	public RemoveActivityOutputPortEdit(Activity<?> activity, OutputPort activityInputPort) {
		super(activity);
		this.activityOutputPort = activityInputPort;
	}

	@Override
	protected void doEditAction(AbstractActivity<?> activity) throws EditException {
		activity.getOutputPorts().remove(activityOutputPort);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getOutputPorts().add(activityOutputPort);
	}

}
