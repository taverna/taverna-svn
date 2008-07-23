/**
 * 
 */
package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class SavedDataflowEvent extends AbstractDataflowEvent {

	public SavedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}