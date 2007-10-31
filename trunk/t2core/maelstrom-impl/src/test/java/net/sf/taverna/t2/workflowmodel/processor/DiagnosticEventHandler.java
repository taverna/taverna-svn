/**
 * 
 */
package net.sf.taverna.t2.workflowmodel.processor;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;

public class DiagnosticEventHandler extends AbstractAnnotatedThing<Port> implements EventHandlingInputPort {

	private static Logger logger = Logger.getLogger(DiagnosticEventHandler.class);
	
	protected int eventCount = 0;

	public synchronized void receiveEvent(WorkflowDataToken token) {
		eventCount++;
		logger.debug(token);
	}

	public int getEventCount() {
		return this.eventCount;
	}

	public synchronized void reset() {
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