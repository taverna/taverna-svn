/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import org.embl.ebi.escience.baclava.*;
import org.jdom.*;
import java.util.*;


/**
 * An AnnotationTemplate object represents a single possible RDF
 * statement that may be generated as a side effect of the invocation
 * of a processor task instance. Any processor may have zero or more
 * of these templates associated with it.
 * @author Tom Oinn
 */
public class AnnotationTemplate {
    
    private List templateComponents = new ArrayList();

    /**
     * Create a blank annotation template object
     */
    public AnnotationTemplate() {
	//
    }
    
    /**
     * Add a new text literal to the template
     */
    public void addLiteral(String text) {
	templateComponents.add(text);
    }

    /**
     * Add a new port reference to this template
     */
    public void addPortReference(Port p) {
	templateComponents.add(new PortReference(p.getName()));
    }

    /**
     * Create an annotation template object from a
     * JDOM Element, the element being the &lt;template&gt;
     * tag.
     */
    public AnnotationTemplate(Element templateElement) {
	super();
	for (Iterator i = templateElement.getChildren().iterator(); i.hasNext();) {
	    Element childElement = (Element)i.next();
	    String name = childElement.getName();
	    if (name.equals("templateLiteral")) {
		addLiteral(childElement.getText());
	    }
	    else if (name.equals("templatePortReference")) {
		templateComponents.add(new PortReference(childElement.getAttributeValue("name")));
	    }
	}
    }
    
    /**
     * The JDOM Element for this AnnotationTemplate
     */
    public Element getElement() {
	Element templateElement = new Element("template", XScufl.XScuflNS);
	for (Iterator i = templateComponents.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof String) {
		Element stringElement = new Element("templateLiteral", XScufl.XScuflNS);
		stringElement.setText((String)o);
		templateElement.addContent(stringElement);
	    }
	    else if (o instanceof PortReference) {
		Element portElement = new Element("templatePortReference", XScufl.XScuflNS);
		portElement.setAttribute("name",((PortReference)o).getName());
		templateElement.addContent(portElement);
	    }
	}
	return templateElement;
    }
    
    class PortReference {
	private String name;
	public PortReference(String name) {
	    this.name = name;
	}
	public String getName() {
	    return this.name;
	}
    }

}
