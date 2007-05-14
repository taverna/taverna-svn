/**
 * 
 */
package net.sf.taverna.t2.workflowmodel.processor;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

class DiagnosticEventHandler implements EventHandlingInputPort {

	protected int eventCount = 0;

	public void receiveEvent(Event e) {
		eventCount++;
		System.out.println(e.toString());
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

}