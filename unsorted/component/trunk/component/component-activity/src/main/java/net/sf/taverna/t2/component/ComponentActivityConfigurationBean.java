package net.sf.taverna.t2.component;

import java.io.Serializable;
import java.net.URL;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Component activity configuration bean.
 * 
 */
public class ComponentActivityConfigurationBean implements Serializable {
	
	private URL registryBase;
	
	private String familyName;
	
	private String componentName;
	
	private Integer componentVersion;
	
	private transient Dataflow dataflow;

	public ComponentActivityConfigurationBean(
			URL registryBase, String familyName, String componentName, Integer componentVersion) {
		super();
		this.registryBase = registryBase;
		this.familyName = familyName;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
	}


	public Dataflow getDataflow() throws ComponentRegistryException {
		if (dataflow == null) {
			ComponentRegistry registry;
			if (registryBase.getProtocol().startsWith("http")) {
				registry = MyExperimentComponentRegistry.getComponentRegistry(registryBase);
			}
			else {
				registry = LocalComponentRegistry.getComponentRegistry(registryBase);
			}
			ComponentFamily family = registry.getComponentFamily(familyName);
			Component component = family.getComponent(componentName);
			ComponentVersion version = component.getComponentVersion(componentVersion);
			dataflow = version.getDataflow();
		}
		return dataflow;
	}


	/**
	 * @return the registryBase
	 */
	public URL getRegistryBase() {
		return registryBase;
	}


	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}


	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}


	/**
	 * @return the componentVersion
	 */
	public Integer getComponentVersion() {
		return componentVersion;
	}
	
	
}
