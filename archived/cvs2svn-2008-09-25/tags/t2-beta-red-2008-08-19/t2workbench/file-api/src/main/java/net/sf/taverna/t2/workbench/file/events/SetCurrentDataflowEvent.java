package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * {@link FileManagerEvent} that means a dataflow has been made current
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class SetCurrentDataflowEvent extends AbstractDataflowEvent {

	public SetCurrentDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}

}
