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
 * Filename           $RCSfile: ElementDef.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-16 15:28:59 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementDef {
	private String elementName;

	private String namespaceURI;

	private String path;	

	private String operation;

	public ElementDef(String elementName, String namespaceURI, String path,
			String operation) {
		if (elementName == null)
			elementName = "*";
		if (path == null)
			path = "*";
		if (operation == null)
			operation = "*";
		this.elementName = elementName;
		this.namespaceURI = namespaceURI;
		this.path = path;		
		this.operation = operation;
	}

	public boolean checkPath(String comparePath) {
		String regexp = path.replaceAll("/[\\*/]*/","/*/"); //resolve multiple continuous wildcards
		regexp = regexp.replaceAll("/\\*/", "/+*");	//convert /*/ to /+*	
		regexp = regexp.replaceAll("\\*", ".*"); //replace any * to .*
		if (regexp.startsWith(".*/")) { //matches if it starts with .*/ or is the start of the path
			regexp=regexp.substring(3,regexp.length());
			Pattern pattern = Pattern.compile(".*/"+regexp);
			Matcher matcher = pattern.matcher(comparePath);
			if (matcher.matches()) return true;
			else {
				pattern=Pattern.compile("^"+regexp);
				matcher=pattern.matcher(comparePath);
				return matcher.matches();
			}
		}
		else {		
			Pattern pattern = Pattern.compile(regexp);
			Matcher matcher = pattern.matcher(comparePath);
			return matcher.matches();
		}
	}

	public String getOperation() {
		return operation;
	}

	public String getPath() {
		return path;
	}

	public String getElementName() {
		return elementName;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public int hashCode() {
//		FIXME: needs to take into account path and operation
		return (getElementName()+getNamespaceURI()).hashCode();
//		return (getElementName() + getNamespaceURI() + getPath() + getOperation())
//				.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass())
			return false;

		ElementDef elDef = (ElementDef) obj;

		//FIXME: needs to take into account path and operation
		return this.getElementName().equals(elDef.getElementName()) && this.getNamespaceURI().equals(elDef.getNamespaceURI());
		
//		return (this.getElementName().equals(elDef.getElementName())
//				&& this.getNamespaceURI().equals(elDef.getNamespaceURI())
//				&& this.getPath().equals(elDef.getPath()) 
//				&& this.getOperation().equals(elDef.getOperation())
//				);
	}

	public String toString() {
		return namespaceURI + ":" + elementName;
	}

}
