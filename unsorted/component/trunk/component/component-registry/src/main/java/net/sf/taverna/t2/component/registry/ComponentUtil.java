/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.net.URL;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;

/**
 * @author alanrw
 *
 */
public class ComponentUtil {
	
	private static Logger logger = Logger.getLogger(ComponentUtil.class);
	
	public static ComponentRegistry calculateRegistry(URL registryBase) throws ComponentRegistryException {
		logger.info("Into calculateRegistry");
		ComponentRegistry registry;
		if (registryBase.getProtocol().startsWith("http")) {
			registry = MyExperimentComponentRegistry.getComponentRegistry(registryBase);
		}
		else {
			registry = LocalComponentRegistry.getComponentRegistry(registryBase);
		}
		logger.info("Finished calculateRegistry");
		return registry;
	}

	public static ComponentFamily calculateFamily(URL registryBase, String familyName) throws ComponentRegistryException {
		logger.info("Into calculateFamily");
		ComponentFamily result = calculateRegistry(registryBase).getComponentFamily(familyName);
		logger.info("Finished calculateFamily");
		return result;
	}
	
	public static Component calculateComponent(URL registryBase, String familyName, String componentName) throws ComponentRegistryException {
		logger.info("Into calculateComponent from parts");
		Component result = calculateFamily(registryBase, familyName).getComponent(componentName);
		logger.info("Finished calculateComponent from parts");
		return result;
	}

	public static ComponentVersion calculateComponentVersion(URL registryBase, String familyName, String componentName, Integer componentVersion) throws ComponentRegistryException {
		logger.info("Into calculateComponentVersion from parts");
		ComponentVersion result = calculateComponent(registryBase, familyName, componentName).getComponentVersion(componentVersion);
		logger.info("Finished calculateComponentVersion from parts");
		return result;
	}

	public static ComponentVersion calculateComponentVersion(ComponentVersionIdentification ident) throws ComponentRegistryException {
		logger.info("Into calculateComponentVersion from id");
		ComponentVersion result = calculateComponentVersion(ident.getRegistryBase(), ident.getFamilyName(), ident.getComponentName(), ident.getComponentVersion());
		logger.info("Finished calculateComponentVersion from id");
		return result;
	}

	public static Component calculateComponent(
			ComponentVersionIdentification ident) throws ComponentRegistryException {
		logger.info("Into calculateComponent from id");
		Component result = calculateComponent(ident.getRegistryBase(), ident.getFamilyName(), ident.getComponentName());
		logger.info("Finished calculateComponent from id");
		return result;
		
	}

	

}
