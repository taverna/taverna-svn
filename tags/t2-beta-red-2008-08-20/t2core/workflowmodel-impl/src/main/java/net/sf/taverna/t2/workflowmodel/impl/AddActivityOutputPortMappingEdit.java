package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class AddActivityOutputPortMappingEdit extends AbstractActivityEdit {

	private final String mappingStart;
	private final String mappingEnd;

	public AddActivityOutputPortMappingEdit(Activity<?> activity, String mappingStart, String mappingEnd) {
		super(activity);
		this.mappingStart = mappingStart;
		this.mappingEnd = mappingEnd;
	}
	@Override
	protected void doEditAction(AbstractActivity<?> activity)
			throws EditException {
		if (activity.getOutputPortMapping().containsKey(mappingStart)) throw new EditException("The mapping starting with:"+mappingStart+" already exists");
		activity.getOutputPortMapping().put(mappingStart, mappingEnd);
	}

	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		activity.getOutputPortMapping().remove(mappingStart);
	}

}
