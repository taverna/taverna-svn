package net.sf.taverna.t2.invocation.impl;

import net.sf.taverna.t2.invocation.WorkflowDataToken;

public interface DataflowInputPortHandler {

	public void inputTokenReceived(WorkflowDataToken token);

}
