package net.sf.taverna.t2.activities.rshell;

import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SymanticTypes;

public class RShellPortSymanticTypeBean {

	private String name;
	
	private SymanticTypes symanticType;

	/**
	 * Returns the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the symanticType.
	 *
	 * @return the symanticType
	 */
	public SymanticTypes getSymanticType() {
		return symanticType;
	}

	/**
	 * Sets the symanticType.
	 *
	 * @param symanticType the new symanticType
	 */
	public void setSymanticType(SymanticTypes symanticType) {
		this.symanticType = symanticType;
	}

}
