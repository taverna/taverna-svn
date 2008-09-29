/*
 *
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */
package uk.ac.man.cs.img.fetaEngine.commons;

import uk.ac.man.cs.img.fetaEngine.util.AbstractEnumeration;

public final class ServiceType extends AbstractEnumeration {
	private String rdfLiteralVal;

	public ServiceType(String toString) {
		super(toString);

	}

	public ServiceType(String toString, String RDFLit) {
		super(toString);
		this.rdfLiteralVal = RDFLit;

	}

	public static final ServiceType UNDEFINED = new ServiceType("","undefined");
	
	public static final ServiceType SOAPLAB = new ServiceType(
			"Soaplab service", "soaplab");

	public static final ServiceType WSDL = new ServiceType("WSDL service",
			"wsdl");

	public static final ServiceType WORKFLOW = new ServiceType(
			"Workflow service", "scufl");

	public static final ServiceType BIOMOBY = new ServiceType(
			"BioMOBY service", "moby");

	public static final ServiceType SEQHOUND = new ServiceType(
			"SeqHound service", "seqhound");

	public static final ServiceType LOCALOBJECT = new ServiceType(
			"Local JAVA Widget", "localjava");

	public static final ServiceType TALISMAN = new ServiceType(
			"Talisman Service", "talisman");

	public static final ServiceType BIOMART = new ServiceType(
			"BioMart Service", "biomart");

	public static final ServiceType BEANSHELL = new ServiceType(
			"BeanShell Service", "beanshell");

	public static final ServiceType INFERNO = new ServiceType(
			"Inferno Service", "inferno");

	public static ServiceType getTypeForString(String typeStr) {

		if (typeStr.equalsIgnoreCase(SOAPLAB.toString())) {
			return SOAPLAB;
		} else if (typeStr.equalsIgnoreCase(WSDL.toString())) {
			return WSDL;
		} else if (typeStr.equalsIgnoreCase(WORKFLOW.toString())) {
			return WORKFLOW;
		} else if (typeStr.equalsIgnoreCase(SEQHOUND.toString())) {
			return SEQHOUND;
		} else if (typeStr.equalsIgnoreCase(BIOMOBY.toString())) {
			return BIOMOBY;
		} else if (typeStr.equalsIgnoreCase(LOCALOBJECT.toString())) {
			return LOCALOBJECT;
		} else if (typeStr.equalsIgnoreCase(TALISMAN.toString())) {
			return TALISMAN;
		} else if (typeStr.equalsIgnoreCase(BIOMART.toString())) {
			return BIOMART;
		} else if (typeStr.equalsIgnoreCase(INFERNO.toString())) {
			return INFERNO;
		} else if (typeStr.equalsIgnoreCase(BEANSHELL.toString())) {
			return BEANSHELL;
		} else {
			return null;
		}

	}

	// Had to add this to accommodate MOBY-MYGRID Service onto

	public static String getRDFLiteralEnumForString(String typeStr) {
		return ServiceType.getTypeForString(typeStr).getRdfLiteralVal();
		/*
		 * if (typeStr.equalsIgnoreCase(SOAPLAB.toString())){ return "soaplab";
		 * }else if (typeStr.equalsIgnoreCase(WSDL.toString())){ return "wsdl";
		 * }else if (typeStr.equalsIgnoreCase(WORKFLOW.toString())){ return
		 * "scufl"; }else if (typeStr.equalsIgnoreCase(SEQHOUND.toString())){
		 * return "moby"; }else if
		 * (typeStr.equalsIgnoreCase(BIOMOBY.toString())){ return "seqhound";
		 * }else if (typeStr.equalsIgnoreCase(LOCALOBJECT.toString())){ return
		 * "localjava"; }else if
		 * (typeStr.equalsIgnoreCase(TALISMAN.toString())){ return "talisman";
		 * }else if (typeStr.equalsIgnoreCase(BIOMART.toString())){ return
		 * "biomart"; }else if (typeStr.equalsIgnoreCase(INFERNO.toString())){
		 * return "inferno"; }else if
		 * (typeStr.equalsIgnoreCase(BEANSHELL.toString())){ return "beanshell";
		 * }else { return null; }
		 */

	}

	public static ServiceType getTypeForRDFLiteralString(String rdfLitStr) {

		if (rdfLitStr.equalsIgnoreCase("soaplab")) {
			return SOAPLAB;
		} else if (rdfLitStr.equalsIgnoreCase("wsdl")) {
			return WSDL;
		} else if (rdfLitStr.equalsIgnoreCase("scufl")) {
			return WORKFLOW;
		} else if (rdfLitStr.equalsIgnoreCase("seqhound")) {
			return SEQHOUND;
		} else if (rdfLitStr.equalsIgnoreCase("moby")) {
			return BIOMOBY;
		} else if (rdfLitStr.equalsIgnoreCase("localjava")) {
			return LOCALOBJECT;
		} else if (rdfLitStr.equalsIgnoreCase("talisman")) {
			return TALISMAN;
		} else if (rdfLitStr.equalsIgnoreCase("biomart")) {
			return BIOMART;
		} else if (rdfLitStr.equalsIgnoreCase("inferno")) {
			return INFERNO;
		} else if (rdfLitStr.equalsIgnoreCase("beanshell")) {
			return BEANSHELL;
		} else {
			return null;
		}

	}

	public String getRdfLiteralVal() {

		return this.rdfLiteralVal;
	}
} // ServiceType
