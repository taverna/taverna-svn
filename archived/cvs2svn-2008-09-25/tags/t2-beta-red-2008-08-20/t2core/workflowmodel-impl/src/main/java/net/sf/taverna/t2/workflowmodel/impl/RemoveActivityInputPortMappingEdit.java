package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class RemoveActivityInputPortMappingEdit extends AbstractActivityEdit {

	private final String processorPortName;
	private String oldValue=null;

	public RemoveActivityInputPortMappingEdit(Activity<?>activity, String processorPortName) {
		super(activity);
		this.processorPortName = processorPortName;
		
	}
	
	@Override
	protected void doEditAction(AbstractActivity<?> activity)
			throws EditException {
		if (!activity.getInputPortMapping().containsKey(processorPortName)) throw new EditException("The input port mapping for the processor port name:"+processorPortName+" doesn't exist");
		oldValue=activity.getInputPortMapping().get(processorPortName);
		activity.getInputPortMapping().remove(processorPortName);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getInputPortMapping().put(processorPortName, oldValue);
	}

}
