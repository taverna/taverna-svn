package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

public abstract class DataflowEditAction extends AbstractAction {

	protected Edits edits = EditsRegistry.getEdits();
	protected EditManager editManager = EditManager.getInstance();
	protected Dataflow dataflow;
	protected Component component;

	public DataflowEditAction(Dataflow dataflow, Component component) {
		this.dataflow = dataflow;
		this.component = component;
	}

}