package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * {@link FileManagerEvent} that means a dataflow has been closed
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class ClosedDataflowEvent extends AbstractDataflowEvent {

	public ClosedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}