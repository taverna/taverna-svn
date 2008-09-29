package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class RemoveActivityOutputPortMappingEdit extends AbstractActivityEdit {

	private final String processorPortName;
	private String oldValue=null;

	public RemoveActivityOutputPortMappingEdit(Activity<?>activity, String processorPortName) {
		super(activity);
		this.processorPortName = processorPortName;
		
	}
	
	@Override
	protected void doEditAction(AbstractActivity<?> activity)
			throws EditException {
		if (!activity.getOutputPortMapping().containsKey(processorPortName)) throw new EditException("The output port mapping for the processor port name:"+processorPortName+" doesn't exist");
		oldValue=activity.getOutputPortMapping().get(processorPortName);
		activity.getOutputPortMapping().remove(processorPortName);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getOutputPortMapping().put(processorPortName, oldValue);
	}

}
