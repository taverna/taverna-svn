/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import java.util.Hashtable;
import java.util.Map;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.rosuda.JRclient.REXP;

/**
 * InputPort specialization for Rserv processor.
 * 
 * Add support for setting the desired javaType on input, so as to convert
 * inputs on workflow invocation.
 * 
 * The syntactic type will be set to l('text/plain') for all but REXP, which
 * will be text/plain.
 * 
 * @author Stian Soiland
 * 
 */
public class RservInputPort extends InputPort {

	public RservInputPort(Processor processor, String name)
			throws DuplicatePortNameException, PortCreationException {
		super(processor, name);
		this.setJavaType("String");
	}

	/*
	 * Possible types for setJavaType
	 */
	public static Map javaTypes;

	static {
		// populate the list of javaTypes. Note that support for new types must
		// also be added to RservTask.javaToRexp
		javaTypes = new Hashtable();
		javaTypes.put("double", double[].class);
		javaTypes.put("int", int[].class);
		javaTypes.put("String", String[].class);
		javaTypes.put("REXP", REXP.class);
	}

	private String javaType;

	/*
	 * Return the javaType this input expects, for instance "double".
	 */
	public String getJavaType() {
		return javaType;
	}

	/*
	 * Set the javaType this input expects. When calling R, input will be
	 * converted to the specified javaType as supported by R. The javaType must
	 * exist as a key in the static map javaTypes. Use setJavaType("REXP") to
	 * pass native REXP instances.
	 */

	public void setJavaType(String type_name) throws IllegalArgumentException {
		Class type = (Class) javaTypes.get(type_name);
		if (type == null) {
			throw new IllegalArgumentException(type_name);
		}
		if (type_name.equals("REXP")) {
			this.setSyntacticType("'text/plain'");
		} else {
			// FIXME: Allow other syntactic types for String
			this.setSyntacticType("l('text/plain')");
		}
		this.javaType = type_name;
	}

}
