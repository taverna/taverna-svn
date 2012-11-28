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
			ComponentVersion version = calculateComponentVersion();
			dataflow = version.getDataflow();
		}
		return dataflow;
	}


	public ComponentVersion calculateComponentVersion() throws ComponentRegistryException {
		Component component = calculateComponent();
		ComponentVersion version = component.getComponentVersion(componentVersion);
		return version;
	}


	public Component calculateComponent() {
		ComponentFamily family = calculateFamily();
		Component component = family.getComponent(componentName);
		return component;
	}


	public ComponentFamily calculateFamily() {
		ComponentRegistry registry;
		registry = calculateRegistry();
		ComponentFamily family = registry.getComponentFamily(familyName);
		return family;
	}


	public ComponentRegistry calculateRegistry() {
		ComponentRegistry registry;
		if (registryBase.getProtocol().startsWith("http")) {
			registry = MyExperimentComponentRegistry.getComponentRegistry(registryBase);
		}
		else {
			registry = LocalComponentRegistry.getComponentRegistry(registryBase);
		}
		return registry;
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
