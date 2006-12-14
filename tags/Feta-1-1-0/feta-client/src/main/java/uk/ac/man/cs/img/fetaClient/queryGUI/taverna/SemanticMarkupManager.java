/*
 * SemanticMarkupManager.java
 *
 * Created on May 12, 2005, 5:12 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import uk.ac.man.cs.img.fetaEngine.commons.FetaOntology;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

/**
 * @author alperp
 * 
 * 
 */
public class SemanticMarkupManager {

	private List serviceTypeComboList;

	private List addedAnchorList;

	private Map listMap;

	private JTree ontologyTree;

	// private FetaRDFSParser2 ontologyParser;
	private FetaOntology fetaOnt;

	private OntologyAnchors ontoAnchors;

	/**
	 * 
	 */
	public SemanticMarkupManager() {
		super();

		listMap = new HashMap();

		addedAnchorList = new ArrayList();
		serviceTypeComboList = new ArrayList();

		serviceTypeComboList.add(ServiceType.WSDL.toString());
		serviceTypeComboList.add(ServiceType.SOAPLAB.toString());
		serviceTypeComboList.add(ServiceType.WORKFLOW.toString());
		serviceTypeComboList.add(ServiceType.BIOMOBY.toString());
		serviceTypeComboList.add(ServiceType.SEQHOUND.toString());
		serviceTypeComboList.add(ServiceType.LOCALOBJECT.toString());
		serviceTypeComboList.add(ServiceType.TALISMAN.toString());
		serviceTypeComboList.add(ServiceType.BIOMART.toString());
		serviceTypeComboList.add(ServiceType.BEANSHELL.toString());
		serviceTypeComboList.add(ServiceType.INFERNO.toString());

		listMap.put(QueryCriteriaType.TYPE_CRITERIA_TYPE, serviceTypeComboList);

		// ONTO-CHNG-PROPGTN
		// listMap.put(QueryCriteriaType.APPLICATION_CRITERIA_TYPE,new
		// ArrayList());
		listMap.put(QueryCriteriaType.TASK_CRITERIA_TYPE, new ArrayList());
		listMap.put(QueryCriteriaType.METHOD_CRITERIA_TYPE, new ArrayList());
		listMap.put(QueryCriteriaType.RESOURCE_CRITERIA_TYPE, new ArrayList());
		// ONTO-CHNG-PROPGTN
		// listMap.put(QueryCriteriaType.RESOURCE_CONTENT_CRITERIA_TYPE, new
		// ArrayList());
		listMap.put(QueryCriteriaType.INPUT_CRITERIA_TYPE, new ArrayList());

		try {

			// load the ontology
			Properties fetaProperties = getProperties();
			if (fetaProperties.containsKey("fetaClient.ontology.URL")) {
				String ontURLAsString = fetaProperties
						.getProperty("fetaClient.ontology.URL");
				try {
					URL ontoURL = new URL(ontURLAsString);
					fetaOnt = new FetaOntology(ontoURL);
				} catch (MalformedURLException mue) {
					System.out
							.println("  - The ontologyURL provided in the properties file is invalid!!");
					System.out
							.println("  - Using the default ontology instead");
					fetaOnt = new FetaOntology();
				}
			} else {
				System.out
						.println("  - NO ontologyURL provided in the properties file!!");
				System.out.println("  - Using the default ontology instead");
				fetaOnt = new FetaOntology();

			}

			FetaRDFSParser2 parser = new FetaRDFSParser2();
			parser.loadRDFSDocument(fetaOnt.getAnnotationOntology(),
					"Feta Annotation Vocabulary");

			// load the anchor terms
			ontoAnchors = new OntologyAnchors();

			visitAllNodes((TreeNode) parser.getRootNode());
		} catch (Exception ex) {
			System.out.println(ex.toString());
			ex.printStackTrace();
		}

		listMap.put(QueryCriteriaType.OUTPUT_CRITERIA_TYPE, listMap
				.get(QueryCriteriaType.INPUT_CRITERIA_TYPE));

	}

	public void visitAllNodes(TreeNode node) {

		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) e
						.nextElement();
				Object iAmBored = n.getUserObject();
				if (iAmBored instanceof FetaOntologyTermModel) {
					FetaOntologyTermModel ontoNode = (FetaOntologyTermModel) iAmBored;
					if ((ontoAnchors.isAnchor(ontoNode.getID()) && !isAnchorAdded(ontoNode
							.getID()))) {
						addedAnchorList.add(ontoNode.getID());
						List anchorTypes = ontoAnchors
								.getTypesForAnchor(ontoNode.getID());
						for (int i = 0; i < anchorTypes.size(); i++) {

							List comboList = (List) listMap
									.get((QueryCriteriaType) anchorTypes.get(i));
							List newList = new ArrayList();
							newList.add(ontoNode);// add the anchor

							addAnchorChildrenToComboList(n, " ", newList); // add
																			// its
																			// children
							comboList.addAll(newList);
							// this functions appends

							listMap.put((QueryCriteriaType) anchorTypes.get(i),
									comboList);

						}
					} else {
						visitAllNodes(n);
					}
				} else {
					visitAllNodes(n);
				}
			}
		}
	}

	public void addAnchorChildrenToComboList(TreeNode node, String prefix,
			List comboList) {

		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) n;
				Object tooBored = dmtn.getUserObject();
				if (tooBored instanceof FetaOntologyTermModel) {

					FetaOntologyTermModel ontoNode = (FetaOntologyTermModel) tooBored;
					ontoNode.setLabel(prefix + ontoNode.getLabel());
					comboList.add(ontoNode);

					// System.out.println("Added to combo list - - - > " +
					// ontoNode.getLabel());
				} else {
					// do nothing

				}

				addAnchorChildrenToComboList(n, prefix + " ", comboList);
			}
		}

	}

	public boolean isAnchorAdded(String anchorID) {
		for (Iterator j = addedAnchorList.iterator(); j.hasNext();) {
			String addedAnchor = (String) j.next();
			if (addedAnchor.equalsIgnoreCase(anchorID))
				return true;
		}
		return false;
	}

	public List getListForCriteriaType(QueryCriteriaType type) {
		if (listMap.containsKey(type)) {
			return (List) listMap.get(type);
		} else {
			return null;
		}
	}

	public static Properties getProperties() throws IOException {
		return FetaClientProperties.getProperties();
	}
}
