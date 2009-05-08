package net.sf.taverna.t2.invocation.impl;

import net.sf.taverna.t2.invocation.WorkflowDataToken;

public interface DataflowOutputPortHandler {

	public void resultTokenProduced(WorkflowDataToken token, String portName);

}
