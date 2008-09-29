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
 *
 */
/******************************************************************
 * File:        XMLConverter.java
 * Created by:  Dave Reynolds
 *
 * $Id: XMLConverter.java,v 1.1 2007-12-14 12:47:46 stain Exp $
 *****************************************************************/
package uk.ac.man.cs.img.fetaEngine.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelRDF;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Utility to convert an XML file to RDF, very limited capability. Assumes a
 * tree structured form with simple element to property mappings, not
 * namespace-aware.
 * <p>
 * To set up the mapping you register XML element names with instances of
 * XMLNodeConverter. The instances are either custom code or ground versions of
 * ConvertLiteral, ConvertURI, ConvertList, ConvertClass.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2007-12-14 12:47:46 $
 */
public class XMLConverter {

	/**
	 * table mapping from XML element names (not yet namespace aware) to
	 * converter blocks
	 */
	Map elementConverters = new HashMap();

	/**
	 * Constructor.
	 */
	public XMLConverter() {
		// Current blank
	}

	/**
	 * Register a convert for a given XML element name.
	 * 
	 * @param element
	 *            the name of the XML element
	 * @param converter
	 *            an XMLConverter instance to handle this case.
	 */
	public void register(String element, XMLNodeConverter converter) {
		elementConverters.put(element, converter);
	}

	/**
	 * Main entry point - parse a file into an RDF model
	 */
	public Model generateRDFModel(Document doc) {
		try {

			Model model = ModelFactory.createDefaultModel();
			NodeList nl = doc.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				parse(nl.item(i), null, model);
			}
			parse(doc, null, model);

			return model;

		} catch (Exception e) {
			// Debug only
			e.printStackTrace();
			throw new ParseException(e.toString());
		}

	}

	/**
	 * Top level recursive call which parses an XML doc subtree.
	 * 
	 * @param node
	 *            the XML dom node to be parsed
	 * @param root
	 *            the RDF resource node which is the parent of the parsed tree
	 * @param model
	 *            the RDF model into which new statements should be inserted
	 * @return the RDF node (Literal or Resource) which represents the parsed
	 *         subtree
	 */
	public RDFNode parse(Node node, Resource root, Model model) {
		RDFNode result = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			String eltTag = node.getNodeName();
			XMLNodeConverter converter = (XMLNodeConverter) elementConverters
					.get(eltTag);
			if (converter != null) {
				result = converter.parse(node, root, model, this);
			}
		}
		return result;
	}

	/**
	 * Get string child of a node
	 */
	public static String getTextValue(Node parent) {
		NodeList nl = parent.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node child = nl.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				String text = child.getNodeValue();
				if (!text.trim().equals("")) {
					return text;
				}
			}
		}
		return null;
	}

	// =======================================================================
	// Inner classes which provide converters for the interesting cases

	/**
	 * Handles case where the body text of an element is mapped to an RDF plain
	 * literal.
	 */
	public static class ConvertLiteral implements XMLNodeConverter {

		/** property which is used to link the literal to the root */
		Property prop;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 */
		public ConvertLiteral(Property prop) {
			this.prop = prop;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String text = getTextValue(node);
			Literal lit = (text != null) ? model.createLiteral(text) : null;
			if (prop != null && root != null && text != null) {
				root.addProperty(prop, lit);
			}
			return lit;
		}

	}

	// =======================================================================

	/**
	 * Handles case where the body text of an element is mapped to an RDF
	 * boolean. An empty body corresponds to "true".
	 */
	public static class ConvertBoolean implements XMLNodeConverter {

		/** property which is used to link the literal to the root */
		Property prop;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 */
		public ConvertBoolean(Property prop) {
			this.prop = prop;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String text = getTextValue(node);
			boolean val = (text == null) || !("false".equalsIgnoreCase(text));
			RDFNode res = null;
			if (prop != null && root != null) {
				res = model.createTypedLiteral(val);
				root.addProperty(prop, res);
			}
			return res;
		}

	}

	// =======================================================================

	/**
	 * Handles case where the body text of an element is mapped to a leaf RDF
	 * resource.
	 */
	public static class ConvertResource implements XMLNodeConverter {

		/** property which is used to link the resource to the root */
		Property prop;

		/** an optional namespace to preprend to the body text to form the URI */
		String ns;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 * @param ns
		 *            an optional namespace to preprend to the body text to form
		 *            the URI
		 */
		public ConvertResource(Property prop, String ns) {
			this.prop = prop;
			this.ns = ns;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String text = getTextValue(node);
			if (ns != null) { /* if (ns != null) text = ns + text; */

				String[] resourceIDParts = text.split("#");
				if (resourceIDParts.length == 2) {
					// it is already namespace qualified
					// do not add namespace
				} else {
					text = ns + text;
				}

			}
			Resource res = model.createResource(text);

			if (prop != null && root != null) {
				root.addProperty(prop, res);
			}
			return res;
		}

	}

	public static class ConvertResourceDoubleProp implements XMLNodeConverter {

		/** property which is used to link the resource to the root */
		Property prop1;

		Property prop2;

		/** an optional namespace to preprend to the body text to form the URI */
		String ns;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 * @param ns
		 *            an optional namespace to preprend to the body text to form
		 *            the URI
		 */
		public ConvertResourceDoubleProp(Property property1,
				Property property2, String ns) {
			this.prop1 = property1;
			this.prop2 = property2;
			this.ns = ns;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String text = getTextValue(node);
			if (ns != null) { /* if (ns != null) text = ns + text; */

				String[] resourceIDParts = text.split("#");
				if (resourceIDParts.length == 2) {
					// it is already namespace qualified
					// do not add namespace
				} else {
					text = ns + text;
				}

			}
			Resource res = model.createResource(text);

			if (prop1 != null && prop2 != null && root != null) {
				root.addProperty(prop1, res);
				root.addProperty(prop2, res);
			}
			return res;
		}

	}

	// =========================================================================

	/**
	 * Handles case where the body text of an element is mapped to a leaf RDF
	 * resource.
	 */
	public static class ConvertTypedResource implements XMLNodeConverter {

		/** property which is used to link the resource to the root */
		Property prop;

		Resource rdfClass;

		/** an optional namespace to preprend to the body text to form the URI */
		String ns;

		Property typingProp;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 * @param ns
		 *            an optional namespace to preprend to the body text to form
		 *            the URI
		 */
		public ConvertTypedResource(Property prop, Property typingProperty,
				Resource classRDF, String ns) {
			this.rdfClass = classRDF;
			this.prop = prop;
			this.ns = ns;
			this.typingProp = typingProperty;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String text = getTextValue(node);
			if (ns != null) { /* if (ns != null) text = ns + text */

				String[] resourceIDParts = text.split("#");
				if (resourceIDParts.length == 2) {
					// it is already namespace qualified
					// do not add namespace
				} else {
					text = ns + text;
				}

			}

			Resource res = model.createResource(text);

			// Create the bNode for this element
			Resource thisRoot = model.createResource();
			if (rdfClass != null) {
				thisRoot.addProperty(RDF.type, rdfClass);
			}

			thisRoot.addProperty(typingProp, res);

			if (prop != null && root != null) {
				root.addProperty(prop, thisRoot);
			}

			return thisRoot;

		}
	}

	// =======================================================================

	/**
	 * Handles case where the element represents an RDF resource which has
	 * associated properties in the XML subtree. The resource will be mapped to
	 * a blank node with an optional type.
	 */
	public static class ConvertClass implements XMLNodeConverter {

		/** property which is used to link the resource to the root */
		Property prop;

		/** an optional RDF Class to assign to the created resource */
		Resource rdfClass;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the property which is used to link the literal to the
		 *            root, null if the parent element is a property node
		 * @param rdfClass
		 *            an optional RDF Class to assign to the created resource
		 */
		public ConvertClass(Property prop, Resource rdfClass) {
			this.prop = prop;
			this.rdfClass = rdfClass;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			// Create the bNode for this element
			Resource thisRoot;
			if ((rdfClass != null)
					&& (rdfClass.getURI()
							.equalsIgnoreCase(FetaModelRDF.serviceDescription
									.getURI()))) {
				thisRoot = model
						.createResource("urn:lsid:www.mygrid.org.uk:serviceDescription:"
								+ UUID.randomUUID().toString());
				thisRoot.addProperty(FetaModelRDF.DC_PATCHED_Identifier,
						"urn:lsid:www.mygrid.org.uk:serviceDescription:"
								+ UUID.randomUUID().toString());
			} else {
				thisRoot = model.createResource();
			}
			if (rdfClass != null) {
				thisRoot.addProperty(RDF.type, rdfClass);
			}
			// Link it in if relevant
			if (prop != null && root != null) {
				root.addProperty(prop, thisRoot);
			}
			// Parse the subtree
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				caller.parse(child, thisRoot, model);
			}
			return thisRoot;
		}

	}

	// =======================================================================

	/**
	 * Handles case where the element represents a relation between the parent
	 * element and one or more child nodes.
	 */
	public static class ConvertProperty implements XMLNodeConverter {

		/** property which is used to link the child resources to the root */
		Property prop;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the property which is used to link the child resources to
		 *            the root
		 */
		public ConvertProperty(Property prop) {
			this.prop = prop;
		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				RDFNode childRes = caller.parse(child, null, model);
				if (prop != null && root != null && childRes != null) {
					root.addProperty(prop, childRes);
				}
			}
			return root;
		}

	}

	// =======================================================================

	/**
	 * Inner class - exception for any parse error.
	 */
	public static class ParseException extends RuntimeException {

		public ParseException(String msg) {
			super(msg);
		}
	}

	/**
	 * Handles case where the body text of an element is mapped to an RDF plain
	 * literal wrt a lookup table supplied.
	 */
	public static class ConvertLiteralWithLookupTable implements
			XMLNodeConverter {

		/** property which is used to link the literal to the root */
		Property prop;

		Map lookupTBL;

		/**
		 * Constructor.
		 * 
		 * @param prop
		 *            the optional property which is used to link the literal to
		 *            the root.
		 */
		public ConvertLiteralWithLookupTable(Property prop, Map lookupTable) {

			this.prop = prop;
			this.lookupTBL = lookupTable;

		}

		/**
		 * Create an RDF resource to represent the given XML node in the model,
		 * including attaching known properties.
		 */
		public RDFNode parse(Node node, Resource root, Model model,
				XMLConverter caller) {
			String textFromXML = getTextValue(node).toLowerCase();
			String textToGo = (String) lookupTBL.get(textFromXML);

			Literal lit = ((textFromXML != null) && (textToGo != null)) ? model
					.createLiteral(textToGo) : null;
			if (prop != null && root != null && textFromXML != null) {
				root.addProperty(prop, lit);
			}
			return lit;
		}

	}

}
