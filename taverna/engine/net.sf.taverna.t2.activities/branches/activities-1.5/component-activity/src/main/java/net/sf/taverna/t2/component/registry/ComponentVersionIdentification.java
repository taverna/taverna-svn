/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import java.net.URL;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.api.Version.ID;

/**
 * @author alanrw
 * 
 */
public class ComponentVersionIdentification implements
		net.sf.taverna.t2.component.api.Version.ID {
	private static final long serialVersionUID = 1768548650702925916L;
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

	public ComponentVersionIdentification(Registry registry, Family family,
			net.sf.taverna.t2.component.api.Component component, Integer version) {
		this(registry.getRegistryBase(), family.getName(), component.getName(), version);
	}

	public ComponentVersionIdentification(Version.ID toBeCopied) {
		this.registryBase = toBeCopied.getRegistryBase();
		this.familyName = toBeCopied.getFamilyName();
		this.componentName = toBeCopied.getComponentName();
		this.componentVersion = toBeCopied.getComponentVersion();
	}

	/**
	 * @return the registryBase
	 */
	@Override
	public URL getRegistryBase() {
		return registryBase;
	}

	/**
	 * @return the familyName
	 */
	@Override
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @return the componentName
	 */
	@Override
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @return the componentVersion
	 */
	@Override
	public Integer getComponentVersion() {
		return componentVersion;
	}

	/**
	 * @param componentVersion
	 *            the componentVersion to set
	 */
	public void setComponentVersion(Integer componentVersion) {
		this.componentVersion = componentVersion;
	}

	/**
	 * @param registryBase
	 *            the registryBase to set
	 */
	public void setRegistryBase(URL registryBase) {
		this.registryBase = registryBase;
	}

	/**
	 * @param familyName
	 *            the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @param componentName
	 *            the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

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
		} else if (!registryBase.toString().equals(other.registryBase.toString()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getComponentName() + " V. " + getComponentVersion()
				+ " in family " + getFamilyName() + " on "
				+ getRegistryBase().toExternalForm();
	}

	@Override
	public boolean mostlyEqualTo(ID id) {
		if (this == id)
			return true;
		if (id == null)
			return false;
		if (getClass() != id.getClass())
			return false;
		ComponentVersionIdentification other = (ComponentVersionIdentification) id;
		if (componentName == null) {
			if (other.componentName != null)
				return false;
		} else if (!componentName.equals(other.componentName))
			return false;
		if (familyName == null) {
			if (other.familyName != null)
				return false;
		} else if (!familyName.equals(other.familyName))
			return false;
		if (registryBase == null) {
			if (other.registryBase != null)
				return false;
		} else if (!registryBase.toString().equals(other.registryBase.toString()))
			return false;
		return true;
	}

	@Override
	public boolean mostlyEqualTo(net.sf.taverna.t2.component.api.Component c) {
		return mostlyEqualTo(new ComponentVersionIdentification(c.getRegistry(), c.getFamily(), c, 0));
	}
}
