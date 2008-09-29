package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Remove the condition serializing execution between the specified control and
 * target processors.
 * 
 * @author Tom Oinn
 * 
 */
public class RemoveConditionEdit extends AbstractBinaryProcessorEdit {

	private ConditionImpl condition = null;

	public RemoveConditionEdit(Processor control, Processor target) {
		super(control, target);

	}

	@Override
	protected void doEditAction(ProcessorImpl control, ProcessorImpl target)
			throws EditException {
		for (ConditionImpl c : control.controlledConditions) {
			if (c.getTarget() == target) {
				this.condition = c;
				break;
			}
		}
		if (this.condition == null) {
			throw new EditException(
					"Can't remove a condition as it doesn't exist");
		}

		control.controlledConditions.remove(condition);
		target.conditions.remove(condition);
	}

	@Override
	protected void undoEditAction(ProcessorImpl control, ProcessorImpl target) {
		control.controlledConditions.add(condition);
		target.conditions.add(condition);
	}

}
