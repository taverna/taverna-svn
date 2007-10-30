/**
 * 
 */
package net.sf.taverna.t2.workflowmodel.processor;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;

public class DiagnosticEventHandler extends AbstractAnnotatedThing<Port> implements EventHandlingInputPort {

	protected int eventCount = 0;

	public void receiveEvent(WorkflowDataToken token) {
		eventCount++;
		System.out.println(token.toString());
	}

	public int getEventCount() {
		return this.eventCount;
	}

	public void reset() {
		this.eventCount = 0;
	}

	public int getDepth() {
		return 0;
	}

	public String getName() {
		return "Test port";
	}

	public Datalink getIncomingLink() {
		// TODO Auto-generated method stub
		return null;
	}

}