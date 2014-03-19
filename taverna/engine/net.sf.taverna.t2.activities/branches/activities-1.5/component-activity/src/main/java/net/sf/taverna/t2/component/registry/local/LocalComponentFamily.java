/**
 *
 */
package net.sf.taverna.t2.component.registry.local;

import static net.sf.taverna.t2.component.registry.local.LocalComponentRegistry.ENC;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
class LocalComponentFamily extends ComponentFamily {
	private static Logger logger = getLogger(LocalComponentFamily.class);
	private static final String PROFILE = "profile";

	private final File componentFamilyDir;

	public LocalComponentFamily(LocalComponentRegistry parentRegistry,
			File componentFamilyDir) {
		super(parentRegistry);
		this.componentFamilyDir = componentFamilyDir;
	}

	@Override
	protected final Profile internalGetComponentProfile()
			throws RegistryException {
		Profile result = null;
		LocalComponentRegistry parentRegistry = (LocalComponentRegistry) this
				.getComponentRegistry();
		File profileFile = new File(componentFamilyDir, PROFILE);
		String profileName;
		try {
			profileName = readFileToString(profileFile, ENC);
		} catch (IOException e) {
			throw new RegistryException("Unable to read profile name", e);
		}
		for (Profile p : parentRegistry.getComponentProfiles())
			if (p.getName().equals(profileName)) {
				result = p;
				break;
			}
		return result;
	}

	@Override
	protected void populateComponentCache() throws RegistryException {
		for (File subFile : componentFamilyDir.listFiles())
			if (subFile.isDirectory()) {
				LocalComponent newComponent = new LocalComponent(subFile,
						(LocalComponentRegistry) this.getComponentRegistry(),
						this);
				componentCache.put(newComponent.getName(), newComponent);
			}
	}

	@Override
	protected final String internalGetName() {
		return componentFamilyDir.getName();
	}

	@Override
	protected final Version internalCreateComponentBasedOn(
			String componentName, String description, Dataflow dataflow)
			throws RegistryException {
		File newSubFile = new File(componentFamilyDir, componentName);
		if (newSubFile.exists()) {
			throw new RegistryException("Component already exists");
		}
		newSubFile.mkdirs();
		File descriptionFile = new File(newSubFile, "description");
		try {
			writeStringToFile(descriptionFile, description, ENC);
		} catch (IOException e) {
			throw new RegistryException("Could not write out description", e);
		}
		LocalComponent newComponent = new LocalComponent(newSubFile,
				(LocalComponentRegistry) this.getComponentRegistry(), this);

		return newComponent.addVersionBasedOn(dataflow, "Initial version");
	}

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
		try {
			if (descriptionFile.isFile())
				return readFileToString(descriptionFile);
		} catch (IOException e) {
			logger.error("failed to get description from " + descriptionFile, e);
		}
		return "";
	}

	@Override
	protected final void internalRemoveComponent(Component component)
			throws RegistryException {
		File componentDir = new File(componentFamilyDir, component.getName());
		try {
			deleteDirectory(componentDir);
		} catch (IOException e) {
			throw new RegistryException("Unable to delete component", e);
		}
	}
}
