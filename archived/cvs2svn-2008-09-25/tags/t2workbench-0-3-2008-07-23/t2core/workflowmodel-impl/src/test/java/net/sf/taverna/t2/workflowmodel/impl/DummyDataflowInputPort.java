package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class DummyDataflowInputPort extends DataflowInputPortImpl {

	public String tokenOwningProcess;
	
	public DummyDataflowInputPort(String name, int depth, int granularDepth,
			Dataflow df) {
		super(name, depth, granularDepth, df);
	}

	@Override
	public void receiveEvent(WorkflowDataToken t) {
		tokenOwningProcess=t.getOwningProcess();
	}
}
