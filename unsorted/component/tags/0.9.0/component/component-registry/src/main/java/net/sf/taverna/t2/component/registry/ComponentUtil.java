/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.net.URL;

import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;

/**
 * @author alanrw
 *
 */
public class ComponentUtil {
	
	public static ComponentRegistry calculateRegistry(URL registryBase) throws ComponentRegistryException {
		ComponentRegistry registry;
		if (registryBase.getProtocol().startsWith("http")) {
			registry = MyExperimentComponentRegistry.getComponentRegistry(registryBase);
		}
		else {
			registry = LocalComponentRegistry.getComponentRegistry(registryBase);
		}
		return registry;
	}

	public static ComponentFamily calculateFamily(URL registryBase, String familyName) throws ComponentRegistryException {
		return calculateRegistry(registryBase).getComponentFamily(familyName);
	}
	
	public static Component calculateComponent(URL registryBase, String familyName, String componentName) throws ComponentRegistryException {
		return calculateFamily(registryBase, familyName).getComponent(componentName);
	}

	public static ComponentVersion calculateComponentVersion(URL registryBase, String familyName, String componentName, Integer componentVersion) throws ComponentRegistryException {
		return calculateComponent(registryBase, familyName, componentName).getComponentVersion(componentVersion);
	}

	public static ComponentVersion calculateComponentVersion(ComponentVersionIdentification ident) throws ComponentRegistryException {
		return calculateComponentVersion(ident.getRegistryBase(), ident.getFamilyName(), ident.getComponentName(), ident.getComponentVersion());
	}

	public static Component calculateComponent(
			ComponentVersionIdentification ident) throws ComponentRegistryException {
		return calculateComponent(ident.getRegistryBase(), ident.getFamilyName(), ident.getComponentName());
		
	}

	

}
