/**
 * 
 */
package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class OpenedDataflowEvent extends AbstractDataflowEvent {

	public OpenedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}