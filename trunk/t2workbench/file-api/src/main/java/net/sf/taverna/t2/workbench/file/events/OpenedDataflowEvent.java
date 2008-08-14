package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * {@link FileManagerEvent} that means a dataflow has been opened
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class OpenedDataflowEvent extends AbstractDataflowEvent {

	public OpenedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}