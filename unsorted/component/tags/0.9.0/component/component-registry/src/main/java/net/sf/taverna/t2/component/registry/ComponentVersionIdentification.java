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



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((componentName == null) ? 0 : componentName.hashCode());
		result = prime
				* result
				+ ((componentVersion == null) ? 0 : componentVersion.hashCode());
		result = prime * result
				+ ((familyName == null) ? 0 : familyName.hashCode());
		result = prime * result
				+ ((registryBase == null) ? 0 : registryBase.hashCode());
		return result;
	}



	/* (non-Javadoc)
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
		ComponentVersionIdentification other = (ComponentVersionIdentification) obj;
		if (componentName == null) {
			if (other.componentName != null)
				return false;
		} else if (!componentName.equals(other.componentName))
			return false;
		if (componentVersion == null) {
			if (other.componentVersion != null)
				return false;
		} else if (!componentVersion.equals(other.componentVersion))
			return false;
		if (familyName == null) {
			if (other.familyName != null)
				return false;
		} else if (!familyName.equals(other.familyName))
			return false;
		if (registryBase == null) {
			if (other.registryBase != null)
				return false;
		} else if (!registryBase.equals(other.registryBase))
			return false;
		return true;
	}

	public String toString() {
		return getComponentName() + " V. " + getComponentVersion() + " in family " + getFamilyName() + " on " + getRegistryBase().toExternalForm();
	}
}
