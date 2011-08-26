package net.sf.taverna.t2.workbench.file.exceptions;

import net.sf.taverna.t2.workflowmodel.Dataflow;


public class UnsavedException extends FileException {

	private final Dataflow dataflow;

	public UnsavedException(Dataflow dataflow) {
		super("Dataflow was not saved: " + dataflow);
		this.dataflow = dataflow;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}


}
