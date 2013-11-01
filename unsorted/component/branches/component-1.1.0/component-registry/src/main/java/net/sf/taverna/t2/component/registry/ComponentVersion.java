/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 * 
 */
public abstract class ComponentVersion implements
		net.sf.taverna.t2.component.api.Version {
	private Integer versionNumber;
	private String description;
	private Component component;

	protected ComponentVersion(Component component) {
		this.component = component;
	}

	@Override
	public final synchronized Integer getVersionNumber() {
		if (versionNumber == null)
			versionNumber = internalGetVersionNumber();
		return versionNumber;
	}

	protected abstract Integer internalGetVersionNumber();

	@Override
	public final synchronized String getDescription() {
		if (description == null)
			description = internalGetDescription();

		return description;
	}

	protected abstract String internalGetDescription();

	@Override
	public final synchronized Dataflow getDataflow() throws RegistryException {
		// Cached in dataflow cache
		return internalGetDataflow();
	}

	protected abstract Dataflow internalGetDataflow() throws RegistryException;

	@Override
	public final Component getComponent() {
		return component;
	}

	@Override
	public ID getID() {
		Component c = getComponent();
		return new ComponentVersionIdentification(c.getRegistry()
				.getRegistryBase(), c.getFamily().getName(), c.getName(),
				getVersionNumber());
	}
}
