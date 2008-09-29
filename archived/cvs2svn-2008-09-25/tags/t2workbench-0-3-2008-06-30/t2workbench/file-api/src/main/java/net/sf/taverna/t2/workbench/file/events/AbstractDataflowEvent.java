/**
 * 
 */
package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public abstract class AbstractDataflowEvent extends FileManagerEvent {
	private final Dataflow dataflow;

	public AbstractDataflowEvent(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}
}