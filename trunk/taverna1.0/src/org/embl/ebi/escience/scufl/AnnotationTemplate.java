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
 * An AnnotationTemplate object represents a single possible RDF statement that
 * may be generated as a side effect of the invocation of a processor task
 * instance. Any processor may have zero or more of these templates associated
 * with it.
 * 
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

	// Namespace for RDF statements
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public static final String TAVERNA_PROVENANCE_NS = "urn:lsid:net.sf.taverna:predicates:";

	/**
	 * Create a basic 'subject verb object' triple in RDF format
	 */
	public static AnnotationTemplate standardTemplate(Port subject,
			String verb, Port object) {
		AnnotationTemplate target = new AnnotationTemplate();
		target.addLiteral("<rdf:Description xmlns:rdf=\"" + RDF_NS
				+ "\" rdf:about=\"");
		target.addPortReference(subject);
		target.addLiteral("\"><" + verb + " rdf:resource=\"");
		target.addPortReference(object);
		target.addLiteral("\"/></rdf:Description>");
		return target;
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
	 * Given two maps of inputName->LSID and outputName->LSID create a
	 * concatenated text string from all the elements in this annotation
	 * template populated with the LSID values. Return null if any of the values
	 * are unknown
	 */
	public String getTextAnnotation(Map inputs, Map outputs) {
		// System.out.println("Attempting to create template :\n
		// "+inputs.toString()+"\n"+" "+outputs.toString());
		StringBuffer sb = new StringBuffer();
		for (Iterator i = templateComponents.iterator(); i.hasNext();) {
			Object component = i.next();
			if (component instanceof String) {
				sb.append((String) component);
			} else if (component instanceof PortReference) {
				String portName = ((PortReference) component).getName();
				if (inputs.containsKey(portName)) {
					sb.append((String) inputs.get(portName));
				} else {
					if (outputs.containsKey(portName)) {
						sb.append((String) outputs.get(portName));
					} else {
						return null;
					}
				}
			}
		}
		// System.out.println("Created template : \n "+sb.toString());
		return sb.toString();
	}

	/**
	 * Create an annotation template object from a JDOM Element, the element
	 * being the &lt;template&gt; tag.
	 */
	public AnnotationTemplate(Element templateElement) {
		super();
		for (Iterator i = templateElement.getChildren().iterator(); i.hasNext();) {
			Element childElement = (Element) i.next();
			String name = childElement.getName();
			if (name.equals("templateLiteral")) {
				addLiteral(childElement.getText());
			} else if (name.equals("templatePortReference")) {
				templateComponents.add(new PortReference(childElement
						.getAttributeValue("name")));
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
				Element stringElement = new Element("templateLiteral",
						XScufl.XScuflNS);
				stringElement.setText((String) o);
				templateElement.addContent(stringElement);
			} else if (o instanceof PortReference) {
				Element portElement = new Element("templatePortReference",
						XScufl.XScuflNS);
				portElement.setAttribute("name", ((PortReference) o).getName());
				templateElement.addContent(portElement);
			}
		}
		return templateElement;
	}

	/**
	 * Return a chunk of HTML showing the unknowns in purple
	 */
	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		for (Iterator i = templateComponents.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof String) {
				sb.append(convert((String) o));
			} else if (o instanceof PortReference) {
				sb.append("<font color=\"purple\">");
				sb.append(((PortReference) o).getName());
				sb.append("</font>");
			}
		}
		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * Take an input string and return the string with all less than characters
	 * replaced with the appropriate XML literal
	 */
	public static String convert(String input) {
		StringBuffer sb = new StringBuffer();
		String[] parts = input.split("<");
		for (int i = 0; i < parts.length; i++) {
			sb.append(parts[i]);
			if ((i + 1) < parts.length) {
				sb.append("&lt;");
			}
		}
		return sb.toString();
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
