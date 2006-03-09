package org.embl.ebi.escience.scuflworkers.wsdl.parser;

/**
 * Base class for all descriptors for type
 * 
 */
public class TypeDescriptor {
	private String name;

	private String type;

	private boolean optional;

	private boolean unbounded;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		int i;
		if ((i = name.lastIndexOf('>')) != -1) {
			this.name = name.substring(i + 1);
		} else {
			this.name = name;
		}
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isUnbounded() {
		return unbounded;
	}

	public void setUnbounded(boolean unbounded) {
		this.unbounded = unbounded;
	}

	public String toString() {
		return name + ":" + type;
	}
}