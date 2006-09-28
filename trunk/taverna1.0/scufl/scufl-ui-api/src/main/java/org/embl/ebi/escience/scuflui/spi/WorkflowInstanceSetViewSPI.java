package org.embl.ebi.escience.scuflui.spi;

import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * Implementors present a view over a set of workflow instance
 * models, creating appropriate progress and result displays.
 * @author Tom
 *
 */
public interface WorkflowInstanceSetViewSPI extends UIComponentSPI {

	/**
	 * To be called when a new workflow instance is added to the
	 * model map in the UIUtils class. A sensible behaviour here might
	 * be to construct a new EnactorInvocation component and add
	 * it to a tabbed or card layout component
	 * @param modelName
	 * @param instance
	 */
	public void newWorkflowInstance(String modelName, WorkflowInstance instance);
	
}
