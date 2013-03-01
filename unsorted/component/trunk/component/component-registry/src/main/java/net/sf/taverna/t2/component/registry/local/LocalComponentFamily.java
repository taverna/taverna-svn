/**
 *
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class LocalComponentFamily implements ComponentFamily {
	
	private static Logger logger = Logger.getLogger(LocalComponentFamily.class);

	private static final String UTF_8 = "utf-8";
	private static final String PROFILE = "profile";
	private final File componentFamilyDir;
	private final ComponentRegistry parentRegistry;
	
	private ComponentProfile componentProfile;
	private Map<String, Component> componentsCache;

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
		if (componentProfile == null) {
		File profileFile = new File(componentFamilyDir, PROFILE);
		String profileName;
		try {
			profileName = FileUtils.readFileToString(profileFile, UTF_8);
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to read profile name", e);
		}
		for (ComponentProfile p : parentRegistry.getComponentProfiles()) {
			if (p.getName().equals(profileName)) {
				componentProfile = p;
				break;
			}
		}
		}
		return componentProfile;
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

		if (componentsCache == null) {
			componentsCache = new HashMap<String, Component>();
		for (File subFile : componentFamilyDir.listFiles()) {
			if (subFile.isDirectory()) {
				LocalComponent newComponent = new LocalComponent(subFile);
				componentsCache.put(newComponent.getName(), newComponent);
			}
		}
		}
		result.addAll(componentsCache.values());
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
			String description,
			Dataflow dataflow) throws ComponentRegistryException {
		if (componentName == null) {
			throw new ComponentRegistryException(("Component name must not be null"));
		}
		if (dataflow == null) {
			throw new ComponentRegistryException(("Dataflow must not be null"));
		}
		File newSubFile = new File(componentFamilyDir, componentName);
		if (newSubFile.exists()) {
			throw new ComponentRegistryException("Component already exists");
		}
		newSubFile.mkdirs();
		File descriptionFile = new File(newSubFile, "description");
		try {
			FileUtils.writeStringToFile(descriptionFile, description, "utf-8");
		} catch (IOException e) {
			throw new ComponentRegistryException("Could not write out description", e);
		}
		LocalComponent newComponent = new LocalComponent(newSubFile);
		
		if (componentsCache == null) {
			getComponents();
		}
		componentsCache.put(componentName, newComponent);
		return newComponent.addVersionBasedOn(dataflow, "Initial version");
	}

	@Override
	public Component getComponent(String componentName) throws ComponentRegistryException {
		if (componentsCache == null) {
			getComponents();
		}
		return (componentsCache.get(componentName));
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

	@Override
	public String getDescription() {
		File descriptionFile = new File(componentFamilyDir, "description");
		if (descriptionFile.isFile()) {
			try {
				return FileUtils.readFileToString(descriptionFile);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return "";
	}

	@Override
	public void removeComponent(Component component)
			throws ComponentRegistryException {
		componentsCache.remove(component.getName());
		File componentDir = new File(componentFamilyDir, component.getName());
		try {
			FileUtils.deleteDirectory(componentDir);
			
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to delete component", e);
		}
		
	}

}
