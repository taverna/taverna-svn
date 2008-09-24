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

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Tom Oinn, EMBL-EBI
 */

/*
 * This class appears here again because when I made use of the one in Taverna
 * it would load-up unwanted RDFS files by default
 */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Parse an RDFS ontology file into a tree to be displayed by some UI component
 * or other. Builds a tree based on DefaultMutableTreeNodes containing the full
 * text from the 'about' property of each Class in the RDFS document. The tree
 * is denormalised, otherwise it wouldn't be a tree...
 * 
 * @author Tom Oinn
 */

/*
 * this parser crashes for real life rdfs files. (eg. protege export of mygrid
 * domain ontology) therefore I made some changes to convert the behaviour of
 * the parser to one which is a "best effort RDFS parsing" I also put my own
 * FetaOntologyTermModel objects rather than resource identifier string into the
 * Tree model @author alperp
 */
public class FetaRDFSParser {
	/**
	 * The RDF Schema namespace
	 */
	static org.jdom.Namespace rdfsNS = org.jdom.Namespace.getNamespace("rdfs",
			"http://www.w3.org/2000/01/rdf-schema#");

	/**
	 * The RDF namespace
	 */
	static org.jdom.Namespace rdfNS = org.jdom.Namespace.getNamespace("rdf",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	public DefaultMutableTreeNode rootNode;

	// an artificial root node
	Map classToParentList;

	Map classToChildList;

	Map idToTermList;

	String ontologyID;

	public FetaRDFSParser() {
		classToParentList = new HashMap();
		classToChildList = new HashMap();
		idToTermList = new HashMap();
		rootNode = new DefaultMutableTreeNode(new FetaOntologyTermModel("ROOT"));
		// an artificial root
	}

	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	public void loadRDFSDocument(InputStream inputStream, String ontologyName) {

		ontologyID = "Ontology:" + ontologyName;
		try {

			// Load the document
			InputStreamReader isr = new InputStreamReader(inputStream);
			org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(
					false);
			org.jdom.Document document = builder.build(isr);
			// Build the map of classes to parents first
			// as this is supplied directly by the document
			List classElements = document.getRootElement().getChildren("Class",
					rdfsNS);
			for (Iterator i = classElements.iterator(); i.hasNext();) {
				org.jdom.Element classElement = (org.jdom.Element) i.next();

				ArrayList parents = new ArrayList();

				FetaOntologyTermModel currentTerm = parseRDFSClassElement(classElement);

				for (Iterator j = classElement
						.getChildren("subClassOf", rdfsNS).iterator(); j
						.hasNext();) {
					// Iterating over all parents
					org.jdom.Element parent = (org.jdom.Element) j.next();
					String parentClassID = parent.getAttributeValue("resource",
							rdfNS);
					parents.add(parentClassID);

				}
				// Handle the case where the class has no parents
				if (classElement.getChildren("subClassOf", rdfsNS).isEmpty()) {
					parents.add(ontologyID);

				}

				classToParentList.put(currentTerm.getID(), parents
						.toArray(new String[0]));
				classToChildList.put(currentTerm.getID(), new ArrayList());

				idToTermList.put(currentTerm.getID(), currentTerm);
			}

			classToChildList.put(ontologyID, new ArrayList());
			classToParentList.put(ontologyID, new String[0]);

			for (Iterator i = classToParentList.keySet().iterator(); i
					.hasNext();) {
				String classID = (String) i.next();
				String[] parentClassIDs = (String[]) classToParentList
						.get(classID);

				for (int j = 0; j < parentClassIDs.length; j++) {
					ArrayList currentChildren = (ArrayList) classToChildList
							.get(parentClassIDs[j]);

					currentChildren.add(classID);
					classToChildList.put(parentClassIDs[j], currentChildren);

				}
			}

			rootNode.add(generateTree(classToChildList, ontologyID));
		} catch (Exception ex) {
			System.out.println("Exception occured whilst loading RDFS! "
					+ ex.getMessage());
			ex.printStackTrace();
		}

	}

	protected FetaOntologyTermModel parseRDFSClassElement(
			org.jdom.Element ontologyDeclaration) {

		String idValue = ontologyDeclaration.getAttributeValue("about", rdfNS);
		if (idValue == null) {
			idValue = ontologyDeclaration.getAttributeValue("ID", rdfNS);
		}
		String identifier = idValue;

		List myList = ontologyDeclaration.getAttributes();

		for (int i = 0; i < myList.size(); i++) {
			org.jdom.Attribute attr = (org.jdom.Attribute) myList.get(i);
			System.out.println(attr.getValue());

		}
		FetaOntologyTermModel ontologyTerm = new FetaOntologyTermModel(
				identifier);

		String label = ontologyDeclaration.getChildText("label", rdfsNS);
		String comment = ontologyDeclaration.getChildText("comment", rdfsNS);

		if (label == null) {
			// do nohing
		} else {
			ontologyTerm.setLabel(label);
		}

		if (comment == null) {
			ontologyTerm
					.setDefinition("No definiton is provided for this term.");

		} else {
			ontologyTerm.setDefinition(comment);
		}

		return ontologyTerm;
	}

	private DefaultMutableTreeNode generateTree(Map classToChildList,
			String classID) {
		DefaultMutableTreeNode theNode = new DefaultMutableTreeNode();

		if (idToTermList.containsKey(classID)) {
			theNode.setUserObject(idToTermList.get(classID));
		} else {

			theNode.setUserObject(new FetaOntologyTermModel(classID));
		}
		ArrayList children = (ArrayList) classToChildList.get(classID);

		for (Iterator i = children.iterator(); i.hasNext();) {
			String childClassID = (String) i.next();
			theNode.add(generateTree(classToChildList, childClassID));
		}
		return theNode;
	}
}