package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class DummyDataflowOutputPort extends DataflowOutputPortImpl {
	
	public DummyDataflowOutputPort(String portName, Dataflow dataflow) {
		super(portName, dataflow);
	}

	public List<ResultListener> getResultListeners() {
		return resultListeners;
	}
}
