/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

import com.sun.javadoc.*;
import org.jdom.*;
import java.util.*;

/**
 * Contains the information that Taverna will use to access a
 * defined subset of the source files supplied to this tool.
 * This is the bean that the tool populates, it then generates
 * XML for Taverna to consume from this data.
 * @author Tom Oinn
 */
public class APIDescription {

    Map classes = new HashMap();
    String name, description;

    /**
     * Initialise with blank name and description
     */
    public APIDescription() {
	this.name = "No name";
	this.description = "No description";
    }
    
    /**
     * Add a pair of ClassDoc / MethodDoc to expose in the API
     */
    public void add(ClassDoc cd, MethodDoc md) {
	// Does the map contain this classdoc yet?
	Set methods = (Set)classes.get(cd);
	if (methods != null) {
	    methods.add(md);
	}
	else {
	    methods = new HashSet();
	    methods.add(md);
	    classes.put(cd, methods);
	}
    }

    /**
     * Get the number of methods from a particular ClassDoc that
     * this description contains for UI hints
     */
    public int getNumberOfMethodsUsed(ClassDoc cd) {
	Set methods = (Set)classes.get(cd);
	if (methods == null) {
	    return 0;
	}
	else {
	    return methods.size();
	}
    }

    /**
     * Remove a MethodDoc (from the specified ClassDoc)
     */
    public void remove(ClassDoc cd, MethodDoc md) {
	Set methods = (Set)classes.get(cd);
	if (methods != null) {
	    methods.remove(md);
	    // If the methods array is now empty remove it
	    if (methods.isEmpty()) {
		classes.remove(cd);
	    }
	}
    }

    /**
     * Is the specified ClassDoc / MethodDoc pair in the
     * description?
     */
    public boolean contains(ClassDoc cd, MethodDoc md) {
	Set methods = (Set)classes.get(cd);
	if (methods == null ||
	    methods.contains(md) == false) {
	    return false;
	}
	else {
	    return true;
	}
    }

    /**
     * Returns a JDom Element object representing the
     * current contents of the APIDescription in XML form
     */
    public Element asXML() {
	Element root = new Element("APIDescription");
	root.setAttribute("name", this.name);
	Element descriptionElement = new Element("Description");
	descriptionElement.setText(this.description);
	root.addContent(descriptionElement);
	Element classListElement = new Element("Classes");
	root.addContent(classListElement);
	for (Iterator i = classes.keySet().iterator(); i.hasNext();) {
	    ClassDoc cd = (ClassDoc)i.next();
	    Set methods = (Set)classes.get(cd);
	    if (methods.isEmpty() == false) {
		Element classElement = new Element("Class");
		Element classDescription = new Element("Description");
		classDescription.setText(cd.commentText());
		classElement.addContent(classDescription);
		Element methodListElement = new Element("Methods");
		classElement.addContent(methodListElement);
		classElement.setAttribute("name",cd.qualifiedName());
		for (Iterator j = methods.iterator(); j.hasNext(); ) {
		    MethodDoc md = (MethodDoc)j.next();
		    Element methodElement = new Element("Method");
		    methodElement.setAttribute("name",md.name());
		    methodElement.setAttribute("type",nameFor(md.returnType()));
		    methodElement.setAttribute("dimension",dimensionOf(md.returnType()));
		    Element methodDescription = new Element("Description");
		    methodDescription.setText(md.commentText());
		    methodElement.addContent(methodDescription);
		    // Add parameters
		    Parameter[] params = md.parameters();
		    for (int k = 0; k < params.length; k++) {
			Parameter p = params[k];
			Element parameterElement = new Element("Parameter");
			parameterElement.setAttribute("name",p.name());
			parameterElement.setAttribute("type",nameFor(p.type()));
			parameterElement.setAttribute("dimension",dimensionOf(p.type()));
			methodElement.addContent(parameterElement);
		    }
		    methodListElement.addContent(methodElement);
		}
		classListElement.addContent(classElement);
	    }
	}
	return root;
    }

    private String nameFor(Type type) {
	ClassDoc cd = type.asClassDoc();
	if (cd == null) {
	    return type.qualifiedTypeName();
	}
	else {
	    return cd.qualifiedName();
	}
    }

    private String dimensionOf(Type type) {
	// type.dimension looks like '[]' or '[][]'
	// for a one dimensional or two dimensional
	// array respectivel, this converts it to a
	// number corresponding to the underlying array
	// depth with 0 meaning a single item
	return ""+type.dimension().length()/2;
    }
    
}
