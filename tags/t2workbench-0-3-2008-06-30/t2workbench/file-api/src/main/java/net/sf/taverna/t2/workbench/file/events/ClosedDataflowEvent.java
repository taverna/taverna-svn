/**
 * 
 */
package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class ClosedDataflowEvent extends AbstractDataflowEvent {

	public ClosedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}