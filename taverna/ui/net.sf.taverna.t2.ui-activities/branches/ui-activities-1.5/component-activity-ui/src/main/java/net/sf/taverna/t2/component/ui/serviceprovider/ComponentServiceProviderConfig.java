package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.URL;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Version;

public class ComponentServiceProviderConfig {
	private URL registryBase;
	private String familyName;

	public ComponentServiceProviderConfig() {
	}

	public ComponentServiceProviderConfig(Family family) {
		registryBase = family.getComponentRegistry().getRegistryBase();
		familyName = family.getName();
	}

	public ComponentServiceProviderConfig(Version.ID ident) {
		registryBase = ident.getRegistryBase();
		familyName = ident.getFamilyName();
	}

	/**
	 * @return the registryBase
	 */
	public URL getRegistryBase() {
		return registryBase;
	}

	/**
	 * @param registryBase
	 *            the registryBase to set
	 */
	public void setRegistryBase(URL registryBase) {
		this.registryBase = registryBase;
	}

	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName
	 *            the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
}
