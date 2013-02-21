/**
 *
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class LocalComponentRegistry implements ComponentRegistry {

	private static Logger logger = Logger.getLogger(LocalComponentRegistry.class);

	private static Map<File, ComponentRegistry> componentRegistries = new HashMap<File, ComponentRegistry>();

	private File baseDir;

	public LocalComponentRegistry(File registryDir) {
		baseDir = registryDir;
	}

	public static ComponentRegistry getComponentRegistry(File registryDir) {
		if (!componentRegistries.containsKey(registryDir)) {
			componentRegistries.put(registryDir, new LocalComponentRegistry(registryDir));
		}
		return componentRegistries.get(registryDir);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.component.registry.ComponentRegistry#createComponentFamily
	 * (java.lang.String, net.sf.taverna.t2.component.profile.ComponentProfile)
	 */
	@Override
	public ComponentFamily createComponentFamily(String name, ComponentProfile componentProfile)
			throws ComponentRegistryException {
		if (name == null) {
			throw new ComponentRegistryException(("Component name must not be null"));
		}
		if (componentProfile == null) {
			throw new ComponentRegistryException(("Component profile must not be null"));
		}
		File newFamilyDir = new File(getComponentFamiliesDir(), name);
		if (newFamilyDir.exists()) {
			throw new ComponentRegistryException(("Component family already exists"));
		}
		newFamilyDir.mkdirs();
		File profileFile = new File(newFamilyDir, "profile");
		try {
			FileUtils.writeStringToFile(profileFile, componentProfile.getName(), "utf-8");
		} catch (IOException e) {
			throw new ComponentRegistryException("Could not write out profile", e);
		}
		return new LocalComponentFamily(this, newFamilyDir);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.component.registry.ComponentRegistry#getComponentFamilies
	 * ()
	 */
	@Override
	public List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		List<ComponentFamily> result = new ArrayList<ComponentFamily>();
		File familiesDir = getComponentFamiliesDir();
		for (File subFile : familiesDir.listFiles()) {
			if (subFile.isDirectory()) {
				result.add(new LocalComponentFamily(this, subFile));
			}
		}
		return result;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.component.registry.ComponentRegistry#getComponentProfiles
	 * ()
	 */
	@Override
	public List<ComponentProfile> getComponentProfiles() {
		List<ComponentProfile> result = new ArrayList<ComponentProfile>();
		File profilesDir = getComponentProfilesDir();
		for (File subFile : profilesDir.listFiles()) {
			if (subFile.isFile() && (!subFile.isHidden()) && subFile.getName().endsWith(".xml")) {
				try {
					ComponentProfile newProfile = new ComponentProfile(subFile.toURI().toURL());
					result.add(newProfile);
				} catch (MalformedURLException e) {
					logger.error("Unable to read profile", e);
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.component.registry.ComponentRegistry#getRegistryBase()
	 */
	@Override
	public URL getRegistryBase() {
		try {
			return getBaseDir().toURI().toURL();
		} catch (MalformedURLException e) {
			logger.error(e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.component.registry.ComponentRegistry#removeComponentFamily
	 * (net.sf.taverna.t2.component.registry.ComponentFamily)
	 */
	@Override
	public void removeComponentFamily(ComponentFamily componentFamily)
			throws ComponentRegistryException {
		File componentFamilyDir = new File(getComponentFamiliesDir(), componentFamily.getName());
		try {
			FileUtils.deleteDirectory(componentFamilyDir);
		} catch (IOException e) {
			throw new ComponentRegistryException("Unable to delete component family", e);
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

	public static ComponentRegistry getComponentRegistry(URL componentRegistryBase) {
		try {
			return getComponentRegistry(new File(componentRegistryBase.toURI()));
		} catch (URISyntaxException e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public ComponentFamily getComponentFamily(String familyName) {
		File componentFamilyDir = new File(getComponentFamiliesDir(), familyName);
		if (componentFamilyDir.exists()) {
			return new LocalComponentFamily(this, componentFamilyDir);
		}
		return null;
	}

	@Override
	public ComponentProfile addComponentProfile(ComponentProfile componentProfile)
			throws ComponentRegistryException {
		if (componentProfile == null) {
			throw new ComponentRegistryException(("Component profile must not be null"));
		}
		String name = componentProfile.getName().replaceAll("\\W+", "") + ".xml";
		String inputString = componentProfile.getXML();
		File outputFile = new File(getComponentProfilesDir(), name);
		try {
			FileUtils.writeStringToFile(outputFile, inputString);
		} catch (IOException e) {
				throw new ComponentRegistryException("Unable to save profile", e);
		}

		try {
			return new ComponentProfile(outputFile.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new ComponentRegistryException("Unable to create profile", e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseDir == null) ? 0 : baseDir.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
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
		LocalComponentRegistry other = (LocalComponentRegistry) obj;
		if (baseDir == null) {
			if (other.baseDir != null)
				return false;
		} else if (!baseDir.equals(other.baseDir))
			return false;
		return true;
	}
}
