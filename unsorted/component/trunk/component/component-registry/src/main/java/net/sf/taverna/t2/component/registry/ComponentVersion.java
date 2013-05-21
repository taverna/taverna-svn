/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public abstract class ComponentVersion {
	
	private Integer versionNumber;
	
	private String description;
	
	private Component component;
	
	protected ComponentVersion(Component component) {
		this.component = component;
	}
	
	public final synchronized Integer getVersionNumber() {
		if (versionNumber == null) {
			versionNumber = internalGetVersionNumber();
		}
		return versionNumber;
	}
	
	protected abstract Integer internalGetVersionNumber();
	
	public final synchronized String getDescription() {
		if (description == null) {
			description = internalGetDescription();
		}
		return description;
	}
	
	protected abstract String internalGetDescription();

	public final synchronized Dataflow getDataflow() throws ComponentRegistryException {
		// Cached in dataflow cache
		return internalGetDataflow();
	}
	
	protected abstract Dataflow internalGetDataflow() throws ComponentRegistryException;
	
	public final Component getComponent() {
		return component;
	}

}
