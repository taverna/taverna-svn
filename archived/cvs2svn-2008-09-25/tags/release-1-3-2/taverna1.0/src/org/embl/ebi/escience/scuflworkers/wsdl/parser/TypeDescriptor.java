package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.util.List;

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

	/**
	 * Translate a java type into a taverna type string
	 */
	public static String translateJavaType(Class type) {
		if (type.equals(String[].class)) {
			return "l('text/plain')";
		} else if (type.equals(org.w3c.dom.Element.class)) {
			return "'text/xml'";
		}

		else if (type.equals(org.w3c.dom.Element[].class)) {
			return "l('text/xml')";
		} else if (type.equals(byte[].class)) {
			return "'application/octet-stream'";
		} else {
			return "'text/plain'";
		}
	}

	public static void retrieveSignature(List params, String[] names, Class[] types) {
		for (int i = 0; i < names.length; i++) {
			TypeDescriptor descriptor = (TypeDescriptor) params.get(i);
			names[i] = descriptor.getName();
			String s = descriptor.getType().toLowerCase();
			if ("string".equals(s)) {
				types[i] = String.class;
			} else if ("arrayof_xsd_string".equalsIgnoreCase(s) || "arrayofstring".equalsIgnoreCase(s)
					|| "arrayof_soapenc_string".equalsIgnoreCase(s)) {
				types[i] = String[].class;

			} else if ("double".equals(s)) {
				types[i] = Double.TYPE;
			} else if ("float".equals(s)) {
				types[i] = Float.TYPE;
			} else if ("int".equals(s)) {
				types[i] = Integer.TYPE;
			} else if ("boolean".equals(s)) {
				types[i] = Boolean.TYPE;
			} else if ("base64binary".equals(s)) {
				types[i] = byte[].class;
			} else {
				types[i] = org.w3c.dom.Element.class;
			}

		}
	}
}