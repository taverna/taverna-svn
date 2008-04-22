package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Rename the specified processor
 * 
 * @author Tom Oinn
 * 
 */
public class RenameProcessorEdit extends AbstractProcessorEdit {

	String newName;

	String oldName = null;

	public RenameProcessorEdit(Processor p, String newName) {
		super(p);
		this.newName = newName;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		oldName = processor.getLocalName();
		processor.setName(newName);
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		processor.setName(oldName);

	}

}
