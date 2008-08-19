
package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * {@link FileManagerEvent} that means a dataflow has been saved
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class SavedDataflowEvent extends AbstractDataflowEvent {

	public SavedDataflowEvent(Dataflow dataflow) {
		super(dataflow);
	}
}