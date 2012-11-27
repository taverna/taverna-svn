/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public interface ComponentVersion {
	
	public Integer getVersionNumber();
	
	public String getDescription();

	public Dataflow getDataflow() throws ComponentRegistryException;
	
	public Component getComponent();

}
