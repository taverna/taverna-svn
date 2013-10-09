/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.net.URL;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentUtil {

	private static Logger logger = Logger.getLogger(ComponentUtil.class);

	public static Registry calculateRegistry(URL registryBase)
			throws RegistryException {
		logger.info("Into calculateRegistry");
		Registry registry;
		if (registryBase.getProtocol().startsWith("http")) {
			// FIXME Assumes that the URL refers to a deployment of myExperiment's old API
			registry = MyExperimentComponentRegistry
					.getComponentRegistry(registryBase);
		} else {
			registry = LocalComponentRegistry
					.getComponentRegistry(registryBase);
		}
		logger.info("Finished calculateRegistry");
		return registry;
	}

	public static Family calculateFamily(URL registryBase, String familyName)
			throws RegistryException {
		logger.info("Into calculateFamily");
		Family result = calculateRegistry(registryBase).getComponentFamily(
				familyName);
		logger.info("Finished calculateFamily");
		return result;
	}

	public static Component calculateComponent(URL registryBase,
			String familyName, String componentName) throws RegistryException {
		logger.info("Into calculateComponent from parts");
		Component result = calculateFamily(registryBase, familyName)
				.getComponent(componentName);
		logger.info("Finished calculateComponent from parts");
		return result;
	}

	public static Version calculateComponentVersion(URL registryBase,
			String familyName, String componentName, Integer componentVersion)
			throws RegistryException {
		logger.info("Into calculateComponentVersion from parts");
		Version result = calculateComponent(registryBase, familyName,
				componentName).getComponentVersion(componentVersion);
		logger.info("Finished calculateComponentVersion from parts");
		return result;
	}

	public static Version calculateComponentVersion(Version.ID ident)
			throws RegistryException {
		logger.info("Into calculateComponentVersion from id");
		Version result = calculateComponentVersion(ident.getRegistryBase(),
				ident.getFamilyName(), ident.getComponentName(),
				ident.getComponentVersion());
		logger.info("Finished calculateComponentVersion from id");
		return result;
	}

	public static Component calculateComponent(Version.ID ident)
			throws RegistryException {
		logger.info("Into calculateComponent from id");
		Component result = calculateComponent(ident.getRegistryBase(),
				ident.getFamilyName(), ident.getComponentName());
		logger.info("Finished calculateComponent from id");
		return result;

	}

	public static Profile makeProfile(URL url) throws RegistryException {
		return new ComponentProfile(url);
	}
}
