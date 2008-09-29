package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Abstract FileManagerEvent that relates to a {@link Dataflow}
 * 
 * @see AbstractDataflowEvent
 * @see ClosedDataflowEvent
 * @see OpenedDataflowEvent
 * @see SavedDataflowEvent
 * @see SetCurrentDataflowEvent
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractDataflowEvent extends FileManagerEvent {
	private final Dataflow dataflow;

	public AbstractDataflowEvent(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}
}