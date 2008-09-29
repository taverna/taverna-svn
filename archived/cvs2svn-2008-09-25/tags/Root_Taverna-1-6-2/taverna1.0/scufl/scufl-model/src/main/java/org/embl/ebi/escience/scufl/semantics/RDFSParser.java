/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.semantics;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Parse an RDFS ontology file into a tree to be displayed by some UI component
 * or other. Builds a tree based on DefaultMutableTreeNodes containing the full
 * text from the 'about' property of each Class in the RDFS document. The tree
 * is denormalised, otherwise it wouldn't be a tree...
 * 
 * @author Tom Oinn
 */
public class RDFSParser {

	/**
	 * The RDF Schema namespace
	 */
	static Namespace rdfsNS = Namespace.getNamespace("rdfs",
			"http://www.w3.org/2000/01/rdf-schema#");

	/**
	 * The RDF namespace
	 */
	static Namespace rdfNS = Namespace.getNamespace("rdf",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	/**
	 * The root node of the tree
	 */
	public static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
			"Available ontologies :");

	/**
	 * Import an ontology into the data model
	 */
	public static void loadRDFSDocument(InputStream docInputStream,
			String ontologyName) {
		try {
			Map classToParentList = new HashMap();
			Map classToChildList = new HashMap();
			// Load the document
			InputStreamReader isr = new InputStreamReader(docInputStream);
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(isr);
			// Build the map of classes to parents first
			// as this is supplied directly by the document
			List classElements = document.getRootElement().getChildren("Class",
					rdfsNS);
			for (Iterator i = classElements.iterator(); i.hasNext();) {
				Element classElement = (Element) i.next();
				String className = classElement.getAttributeValue("about",
						rdfNS);
				ArrayList parents = new ArrayList();
				for (Iterator j = classElement
						.getChildren("subClassOf", rdfsNS).iterator(); j
						.hasNext();) {
					// Iterating over all parents
					Element parent = (Element) j.next();
					String parentClassName = parent.getAttributeValue(
							"resource", rdfNS);
					parents.add(parentClassName);
				}
				// Handle the case where the class has no parents
				if (classElement.getChildren("subClassOf", rdfsNS).isEmpty()) {
					parents.add("root:" + ontologyName);
				}
				// Stuff it into the hash
				classToParentList
						.put(className, parents.toArray(new String[0]));
				// Also put a placeholder in the other hash, we'll populate it
				// later
				classToChildList.put(className, new ArrayList());
			}
			classToChildList.put("root:" + ontologyName, new ArrayList());
			classToParentList.put("root:" + ontologyName, new String[0]);
			// Iterate over all the classes just created and create the arrays
			// of
			// children for each one, which is required for the tree generation.
			for (Iterator i = classToParentList.keySet().iterator(); i
					.hasNext();) {
				String className = (String) i.next();
				String[] parentClassNames = (String[]) classToParentList
						.get(className);
				for (int j = 0; j < parentClassNames.length; j++) {
					ArrayList currentChildren = (ArrayList) classToChildList
							.get(parentClassNames[j]);
					currentChildren.add(className);
				}
			}
			// Recursively generate the tree
			rootNode.add(generateTree(classToChildList, "root:" + ontologyName,
					false));

		} catch (Exception ex) {
			System.out.println("Exception occured whilst loading RDFS! "
					+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	private static DefaultMutableTreeNode generateTree(Map classToChildList,
			String className, boolean generateHolder) {
		DefaultMutableTreeNode theNode = null;
		if (generateHolder) {
			theNode = new DefaultMutableTreeNode(new RDFSClassHolder(className));
		} else {
			theNode = new DefaultMutableTreeNode(className);
		}
		ArrayList children = (ArrayList) classToChildList.get(className);
		for (Iterator i = children.iterator(); i.hasNext();) {
			String childClassName = (String) i.next();
			theNode.add(generateTree(classToChildList, childClassName, true));
		}
		return theNode;
	}

}
