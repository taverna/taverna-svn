package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

public class MapProcessorPortForActivityEdit implements Edit<Processor> {

	private final ProcessorImpl processor;
	CompoundEdit compoundEdit = null;
	

	public MapProcessorPortForActivityEdit(Processor processor) {
		this.processor = (ProcessorImpl)processor;
	}
	
	public Processor doEdit() throws EditException {
		EditsImpl editImpl = new EditsImpl();
		List<Edit<?>> edits = new ArrayList<Edit<?>>();
		
		
		
		compoundEdit = new CompoundEdit(edits);
		compoundEdit.doEdit();
		return processor;
	}

	public Object getSubject() {
		return processor;
	}

	public boolean isApplied() {
		return (compoundEdit!=null && compoundEdit.isApplied());
	}

	public void undo() {
		compoundEdit.undo();
	}

}
