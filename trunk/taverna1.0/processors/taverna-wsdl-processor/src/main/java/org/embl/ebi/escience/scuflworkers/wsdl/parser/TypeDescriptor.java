/*
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
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: TypeDescriptor.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-08-18 14:53:45 $
 *               by   $Author: sowen70 $
 * Created on March-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Base class for all descriptors for type
 * 
 */
public class TypeDescriptor {
	private String name;

	private String type;

	private boolean optional;

	private boolean unbounded;

	private QName qname;

	public QName getQname() {
		if (qname != null)
			return qname;
		else {
			return new QName("", type);
		}
	}

	public void setQnameFromString(String qname) {
		String[] split = qname.split("}");
		if (split.length == 1) {
			this.qname = new QName("", qname);
		} else {
			String uri = split[0];
			uri = uri.replaceAll("\\{", "");
			uri = uri.replaceAll("\\}", "");
			this.qname = new QName(uri, split[1]);
		}
	}

	public void setQname(QName qname) {
		this.qname = qname;
	}

	public String getNamespaceURI() {
		return getQname().getNamespaceURI();
	}

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

	public static void retrieveSignature(List params, String[] names,
			Class[] types) {
		for (int i = 0; i < names.length; i++) {
			TypeDescriptor descriptor = (TypeDescriptor) params.get(i);
			names[i] = descriptor.getName();
			String s = descriptor.getType().toLowerCase();

			if (descriptor instanceof ArrayTypeDescriptor) {
				if (((ArrayTypeDescriptor)descriptor).getElementType() instanceof BaseTypeDescriptor) {
					types[i] = String[].class;
				}
				else {				
					types[i] = org.w3c.dom.Element.class;
				}
			}
			else {
				if ("string".equals(s)) {				
						types[i] = String.class;
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

	/**
	 * Determines whether the descriptor describes a data structure that is
	 * cyclic, i.e. contains inner elements that contain references to outer
	 * elements, leading to a state of infinate recursion.
	 * 
	 * @param descriptor
	 * @return
	 */
	public static boolean isCyclic(TypeDescriptor descriptor) {
		boolean result = false;
		if (!(descriptor instanceof BaseTypeDescriptor)) {
			if (descriptor instanceof ComplexTypeDescriptor) {
				result = testForCyclic((ComplexTypeDescriptor) descriptor,
						new ArrayList());
			} else {
				result = testForCyclic((ArrayTypeDescriptor) descriptor,
						new ArrayList());
			}
		}
		return result;
	}

	private static boolean testForCyclic(ComplexTypeDescriptor descriptor,
			List parents) {
		boolean result = false;
		String descKey = descriptor.getQname().toString();
		if (parents.contains(descKey))
			result = true;
		else {
			parents.add(descKey);
			List elements = descriptor.getElements();
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				TypeDescriptor elementDescriptor = (TypeDescriptor) iterator
						.next();
				if (elementDescriptor instanceof ComplexTypeDescriptor) {
					result = testForCyclic(
							(ComplexTypeDescriptor) elementDescriptor, parents);
				} else if (elementDescriptor instanceof ArrayTypeDescriptor) {
					result = testForCyclic(
							(ArrayTypeDescriptor) elementDescriptor, parents);
				}

				if (result)
					break;
			}

			parents.remove(descKey);
		}
		return result;
	}

	private static boolean testForCyclic(ArrayTypeDescriptor descriptor,
			List parents) {
		boolean result = false;
		String descKey = descriptor.getQname().toString();
		if (parents.contains(descKey))
			result = true;
		else {
			parents.add(descKey);

			TypeDescriptor elementDescriptor = (TypeDescriptor) descriptor
					.getElementType();
			if (elementDescriptor instanceof ComplexTypeDescriptor) {
				result = testForCyclic(
						(ComplexTypeDescriptor) elementDescriptor, parents);
			} else if (elementDescriptor instanceof ArrayTypeDescriptor) {
				result = testForCyclic((ArrayTypeDescriptor) elementDescriptor,
						parents);
			}

			parents.remove(descKey);
		}
		return result;
	}
}