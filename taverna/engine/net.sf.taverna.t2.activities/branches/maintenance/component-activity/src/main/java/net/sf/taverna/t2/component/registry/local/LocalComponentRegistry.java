/**
 *
 */
package net.sf.taverna.t2.component.registry.local;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentRegistry;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
class LocalComponentRegistry extends ComponentRegistry {
	private static final Logger logger = getLogger(LocalComponentRegistry.class);
	static final String ENC = "utf-8";

	private File baseDir;

	@SuppressWarnings("unused")
	private static final String BASE_PROFILE_ID = "http://purl.org/wfever/workflow-base-profile";
	@SuppressWarnings("unused")
	private static final String BASE_PROFILE_FILENAME = "BaseProfile.xml";

	public LocalComponentRegistry(File registryDir) throws RegistryException {
		super(registryDir);
		baseDir = registryDir;
	}

	@Override
	public Family internalCreateComponentFamily(String name,
			Profile componentProfile, String description, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		File newFamilyDir = new File(getComponentFamiliesDir(), name);
		newFamilyDir.mkdirs();
		File profileFile = new File(newFamilyDir, "profile");
		try {
			writeStringToFile(profileFile, componentProfile.getName(), ENC);
		} catch (IOException e) {
			throw new RegistryException("Could not write out profile", e);
		}
		File descriptionFile = new File(newFamilyDir, "description");
		try {
			writeStringToFile(descriptionFile, description, ENC);
		} catch (IOException e) {
			throw new RegistryException("Could not write out description", e);
		}
		return new LocalComponentFamily(this, newFamilyDir);
	}

	@Override
	protected void populateFamilyCache() throws RegistryException {
		File familiesDir = getComponentFamiliesDir();
		for (File subFile : familiesDir.listFiles())
			if (subFile.isDirectory()) {
				LocalComponentFamily newFamily = new LocalComponentFamily(this,
						subFile);
				familyCache.put(newFamily.getName(), newFamily);
			}
	}

	@Override
	protected void populateProfileCache() throws RegistryException {
		File profilesDir = getComponentProfilesDir();
		for (File subFile : profilesDir.listFiles())
			if (subFile.isFile() && (!subFile.isHidden())
					&& subFile.getName().endsWith(".xml"))
				try {
					Profile newProfile = new ComponentProfile(this,
							subFile.toURI());
					profileCache.add(newProfile);
				} catch (MalformedURLException e) {
					logger.error("Unable to read profile", e);
				}
	}

	@Override
	protected void internalRemoveComponentFamily(Family componentFamily)
			throws RegistryException {
		File componentFamilyDir = new File(getComponentFamiliesDir(),
				componentFamily.getName());
		try {
			deleteDirectory(componentFamilyDir);
		} catch (IOException e) {
			throw new RegistryException("Unable to delete component family", e);
		}
	}

	private File getBaseDir() {
		baseDir.mkdirs();
		return baseDir;
	}

	private File getComponentFamiliesDir() {
		File componentFamiliesDir = new File(getBaseDir(), "componentFamilies");
		componentFamiliesDir.mkdirs();
		return componentFamiliesDir;
	}

	private File getComponentProfilesDir() {
		File componentProfilesDir = new File(getBaseDir(), "componentProfiles");
		componentProfilesDir.mkdirs();
		return componentProfilesDir;
	}

	@Override
	public Profile internalAddComponentProfile(Profile componentProfile,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		String name = componentProfile.getName().replaceAll("\\W+", "")
				+ ".xml";
		String inputString = componentProfile.getXML();
		File outputFile = new File(getComponentProfilesDir(), name);
		try {
			writeStringToFile(outputFile, inputString);
		} catch (IOException e) {
			throw new RegistryException("Unable to save profile", e);
		}

		try {
			Profile newProfile = new ComponentProfile(this, outputFile.toURI());
			return newProfile;
		} catch (MalformedURLException e) {
			throw new RegistryException("Unable to create profile", e);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseDir == null) ? 0 : baseDir.hashCode());
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
		LocalComponentRegistry other = (LocalComponentRegistry) obj;
		if (baseDir == null) {
			if (other.baseDir != null)
				return false;
		} else if (!baseDir.equals(other.baseDir))
			return false;
		return true;
	}

	@Override
	public void populatePermissionCache() {
		return;
	}

	@Override
	public void populateLicenseCache() {
		return;
	}

	@Override
	public License getPreferredLicense() {
		return null;
	}

	@Override
	public Set<Version.ID> searchForComponents(String prefixString, String text)
			throws RegistryException {
		throw new RegistryException("Local registries cannot be searched yet");
	}

	@Override
	public String getRegistryTypeName() {
		return "File System";
	}
}
