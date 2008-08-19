package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class AddActivityInputPortMappingEdit extends AbstractActivityEdit {

	private final String mappingStart;
	private final String mappingEnd;

	public AddActivityInputPortMappingEdit(Activity<?> activity, String processorPortName, String activityPortName) {
		super(activity);
		this.mappingStart = processorPortName;
		this.mappingEnd = activityPortName;
	}
	@Override
	protected void doEditAction(AbstractActivity<?> activity)
			throws EditException {
		if (activity.getInputPortMapping().containsKey(mappingStart)) throw new EditException("The output mapping for processor name:"+mappingStart+" already exists");
		activity.getInputPortMapping().put(mappingStart, mappingEnd);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getInputPortMapping().remove(mappingStart);
	}

}
