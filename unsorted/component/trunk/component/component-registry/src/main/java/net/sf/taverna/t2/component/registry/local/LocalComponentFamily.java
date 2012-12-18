/**
 * 
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class LocalComponentFamily implements ComponentFamily {

	private final File componentFamilyDir;
	private final ComponentRegistry parentRegistry;

	public LocalComponentFamily(ComponentRegistry parentRegistry, File componentFamilyDir) {
		this.parentRegistry = parentRegistry;
		this.componentFamilyDir = componentFamilyDir;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getComponentProfile()
	 */
	@Override
	public ComponentProfile getComponentProfile()
			throws ComponentRegistryException {
		File profileFile = new File(componentFamilyDir, "profile");
		String profileName;
		try {
			profileName = FileUtils.readFileToString(profileFile, "utf-8");
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to read profile name", e);
		}
		for (ComponentProfile p : parentRegistry.getComponentProfiles()) {
			if (p.getName().equals(profileName)) {
				return p;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getComponentRegistry()
	 */
	@Override
	public ComponentRegistry getComponentRegistry() {
		return parentRegistry;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getComponents()
	 */
	@Override
	public List<Component> getComponents() throws ComponentRegistryException {
		// Assume all directories are components
		List<Component> result = new ArrayList<Component>();
		
		for (File subFile : componentFamilyDir.listFiles()) {
			if (subFile.isDirectory()) {
				LocalComponent newComponent = new LocalComponent(subFile);
				result.add(newComponent);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getName()
	 */
	@Override
	public String getName() {
		return componentFamilyDir.getName();
	}


	@Override
	public ComponentVersion createComponentBasedOn(String componentName,
			Dataflow dataflow) throws ComponentRegistryException {
		File newSubFile = new File(componentFamilyDir, componentName);
		if (newSubFile.exists()) {
			throw new ComponentRegistryException("Component already exists");
		}
		newSubFile.mkdirs();
		LocalComponent newComponent = new LocalComponent(newSubFile);
		return newComponent.addVersionBasedOn(dataflow);
	}

	@Override
	public Component getComponent(String componentName) {
		File componentDir = new File(componentFamilyDir, componentName);
		if (componentDir.exists()) {
			return new LocalComponent(componentDir);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((componentFamilyDir == null) ? 0 : componentFamilyDir
						.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalComponentFamily other = (LocalComponentFamily) obj;
		if (componentFamilyDir == null) {
			if (other.componentFamilyDir != null)
				return false;
		} else if (!componentFamilyDir.equals(other.componentFamilyDir))
			return false;
		return true;
	}

}
