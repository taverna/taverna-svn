package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.URL;

import org.apache.log4j.Logger;

public class ComponentServiceProviderConfig {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger
	.getLogger(ComponentServiceProviderConfig.class);
	
	private URL registryBase;
	
	private String familyName;
	
	public ComponentServiceProviderConfig() {
		super();

	}

	/**
	 * @return the registryBase
	 */
	public URL getRegistryBase() {
		return registryBase;
	}

	/**
	 * @param registryBase the registryBase to set
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
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

}
