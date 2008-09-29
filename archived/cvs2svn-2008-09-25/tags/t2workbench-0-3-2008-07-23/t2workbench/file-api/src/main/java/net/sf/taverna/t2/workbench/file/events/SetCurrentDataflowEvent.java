package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class SetCurrentDataflowEvent extends AbstractDataflowEvent {

	public SetCurrentDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}

}
