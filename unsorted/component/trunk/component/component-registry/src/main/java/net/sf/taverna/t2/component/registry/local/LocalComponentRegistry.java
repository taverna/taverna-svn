/**
 * 
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.IOException;
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentRegistry#createComponentFamily(java.lang.String, net.sf.taverna.t2.component.profile.ComponentProfile)
	 */
	@Override
	public ComponentFamily createComponentFamily(String name,
			ComponentProfile componentProfile)
			throws ComponentRegistryException {
		File newFamilyDir = new File(getComponentFamiliesDir(), name);
		if (newFamilyDir.exists()) {
			throw new ComponentRegistryException(("Component family already exists"));
		}
		newFamilyDir.mkdirs();
		File profileFile = new File(newFamilyDir, "profile");
		try {
			if (componentProfile != null) {
				FileUtils.writeStringToFile(profileFile, componentProfile.getName(), "utf-8");
			}
		} catch (IOException e) {
			throw new ComponentRegistryException("Could not write out profile", e);
		}
		return new LocalComponentFamily(this, newFamilyDir);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentRegistry#getComponentFamilies()
	 */
	@Override
	public List<ComponentFamily> getComponentFamilies()
			throws ComponentRegistryException {
		List<ComponentFamily> result = new ArrayList<ComponentFamily>();
		File familiesDir = getComponentFamiliesDir();
		for (File subFile : familiesDir.listFiles()) {
			if (subFile.isDirectory()) {
					result.add(new LocalComponentFamily(this, subFile));
			}
		}
		return result;
		
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentRegistry#getComponentProfiles()
	 */
	@Override
	public List<ComponentProfile> getComponentProfiles() {
		List<ComponentProfile> result = new ArrayList<ComponentProfile>();
		File profilesDir = getComponentProfilesDir();
		for (File subFile : profilesDir.listFiles()) {
			if (subFile.isFile()) {
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentRegistry#getRegistryBase()
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentRegistry#removeComponentFamily(net.sf.taverna.t2.component.registry.ComponentFamily)
	 */
	@Override
	public void removeComponentFamily(ComponentFamily componentFamily) throws ComponentRegistryException {
		File componentFamilyDir = new File(getComponentFamiliesDir(), componentFamily.getName());
		componentFamilyDir.delete();
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

	public static ComponentRegistry getComponentRegistry(
			URL componentRegistryBase) {
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
}
