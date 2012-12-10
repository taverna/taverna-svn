/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.net.URL;

/**
 * @author alanrw
 *
 */
public class ComponentVersionIdentification {
	
	private URL registryBase;
	
	private String familyName;
	
	private String componentName;
	
	private Integer componentVersion;
	
	public ComponentVersionIdentification(URL registryBase, String familyName,
			String componentName, Integer componentVersion) {
		super();
		this.registryBase = registryBase;
		this.familyName = familyName;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
	}



	public ComponentVersionIdentification(
			ComponentVersionIdentification toBeCopied) {
		this.registryBase = toBeCopied.getRegistryBase();
		this.familyName = toBeCopied.getFamilyName();
		this.componentName = toBeCopied.getComponentName();
		this.componentVersion = toBeCopied.getComponentVersion();
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



	/**
	 * @param componentVersion the componentVersion to set
	 */
	public void setComponentVersion(Integer componentVersion) {
		this.componentVersion = componentVersion;
	}



	/**
	 * @param registryBase the registryBase to set
	 */
	public void setRegistryBase(URL registryBase) {
		this.registryBase = registryBase;
	}



	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}



	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}


}
