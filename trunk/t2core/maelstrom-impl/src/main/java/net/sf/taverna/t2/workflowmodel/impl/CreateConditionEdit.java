package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

public class CreateConditionEdit extends AbstractBinaryProcessorEdit {

	private ConditionImpl condition;
	
	CreateConditionEdit(Processor control, Processor target) {
		super(control, target);
	}
	
	@Override
	protected void doEditAction(ProcessorImpl control, ProcessorImpl target) throws EditException {
		condition = new ConditionImpl(control, target);
		// Check for duplicates
		for (Condition c : control.controlledConditions) {
			if (c.getTarget() == target) {
				throw new EditException("Attempt to create duplicate condition");
			}
		}
		control.controlledConditions.add(condition);
		target.conditions.add(condition);
	}

	@Override
	protected void undoEditAction(ProcessorImpl control, ProcessorImpl target) {
		control.controlledConditions.remove(condition);
		target.conditions.remove(condition);
	}

}
