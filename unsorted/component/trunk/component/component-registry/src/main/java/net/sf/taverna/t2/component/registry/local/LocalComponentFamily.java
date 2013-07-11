/**
 *
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.IOException;

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
public class LocalComponentFamily extends ComponentFamily {
	
	private static Logger logger = Logger.getLogger(LocalComponentFamily.class);

	private static final String UTF_8 = "utf-8";
	private static final String PROFILE = "profile";
	private final File componentFamilyDir;

	public LocalComponentFamily(ComponentRegistry parentRegistry, File componentFamilyDir) {
		super(parentRegistry);
		this.componentFamilyDir = componentFamilyDir;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getComponentProfile()
	 */
	@Override
	public final ComponentProfile internalGetComponentProfile()
			throws ComponentRegistryException {
		ComponentProfile result = null;
		LocalComponentRegistry parentRegistry = (LocalComponentRegistry) this.getComponentRegistry();
		File profileFile = new File(componentFamilyDir, PROFILE);
		String profileName;
		try {
			profileName = FileUtils.readFileToString(profileFile, UTF_8);
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to read profile name", e);
		}
		for (ComponentProfile p : parentRegistry.getComponentProfiles()) {
			if (p.getName().equals(profileName)) {
				result = p;
				break;
			}
		}
		return result;
	}

	protected void populateComponentCache() throws ComponentRegistryException {

		for (File subFile : componentFamilyDir.listFiles()) {
			if (subFile.isDirectory()) {
				LocalComponent newComponent = new LocalComponent(subFile);
				componentCache.put(newComponent.getName(), newComponent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentFamily#getName()
	 */
	@Override
	protected final String internalGetName() {
		return componentFamilyDir.getName();
	}


	@Override
	protected final ComponentVersion internalCreateComponentBasedOn(String componentName,
			String description,
			Dataflow dataflow) throws ComponentRegistryException {

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

		return newComponent.addVersionBasedOn(dataflow, "Initial version");
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
	protected final String internalGetDescription() {
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
	protected final void internalRemoveComponent(Component component)
			throws ComponentRegistryException {
		File componentDir = new File(componentFamilyDir, component.getName());
		try {
			FileUtils.deleteDirectory(componentDir);
			
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to delete component", e);
		}
		
	}

}
