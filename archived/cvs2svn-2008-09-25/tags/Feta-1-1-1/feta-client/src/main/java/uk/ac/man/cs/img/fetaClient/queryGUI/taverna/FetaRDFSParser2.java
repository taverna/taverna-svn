package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import uk.ac.man.cs.img.fetaEngine.commons.FetaOntology;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FetaRDFSParser2 {

	public DefaultMutableTreeNode rootNode;

	// an artificial root node
	Map classToParentList;

	Map classToChildList;

	Map idToTermList;

	String ontologyID;

	public FetaRDFSParser2() {
		// classToParentList = new HashMap();
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

			Model m1 = ModelFactory.createDefaultModel();
			m1.read(inputStream, null);

			Set childOfSomeClass = new HashSet();
			Set parentOfSomeClass = new HashSet();
			Set allClasses = new HashSet();
			Set topNodes = new HashSet();
			Set middleParents = new HashSet();

			// in an RDFS based ontology the subClassOf predicates is used to
			// define the
			// hierarchy. And this is the hierarchy we would like to display

			Selector selector = new SimpleSelector(null, RDFS.subClassOf,
					(RDFNode) null);
			StmtIterator iter1 = (StmtIterator) m1.listStatements(selector);

			if (iter1.hasNext()) {

				while (iter1.hasNext()) {

					Statement stmt = iter1.nextStatement();
					Resource s = (Resource) stmt.getSubject();
					Resource o = (Resource) stmt.getObject();

					if (!allClasses.contains(s.getURI())) {
						allClasses.add(s.getURI());
						idToTermList.put(s.getURI(), parseRDFSClassElement(m1,
								s));
					}
					if (!allClasses.contains(o.getURI())) {
						allClasses.add(o.getURI());
						idToTermList.put(o.getURI(), parseRDFSClassElement(m1,
								o));
					}

					childOfSomeClass.add(s.getURI());
					parentOfSomeClass.add(o.getURI());

				}

				// ths would leave out the one that do not have a parent
				// i.e. top nodes

				topNodes.addAll(parentOfSomeClass);
				topNodes.removeAll(childOfSomeClass);

				middleParents.addAll(parentOfSomeClass);
				middleParents.removeAll(topNodes);

			}

			ArrayList childrenOfRoot = new ArrayList((Collection) topNodes);
			classToChildList.put(ontologyID, childrenOfRoot);

			Iterator iterTops = topNodes.iterator();
			if (iterTops.hasNext()) {
				while (iterTops.hasNext()) {
					String topNodeURI = (String) iterTops.next();

					ResIterator iterr = (ResIterator) m1
							.listSubjectsWithProperty(RDFS.subClassOf,
									(RDFNode) m1.getResource(topNodeURI));
					ArrayList children = new ArrayList();
					if (iterr.hasNext()) {
						while (iterr.hasNext()) {
							children.add(iterr.nextResource().getURI());
						}

					}
					classToChildList.put(topNodeURI, children);
				}
			}

			Iterator iterMiddles = middleParents.iterator();
			if (iterMiddles.hasNext()) {
				while (iterMiddles.hasNext()) {
					String midNodeURI = (String) iterMiddles.next();
					ResIterator iterr3 = (ResIterator) m1
							.listSubjectsWithProperty(RDFS.subClassOf,
									(RDFNode) m1.getResource(midNodeURI));
					ArrayList children2 = new ArrayList();
					if (iterr3.hasNext()) {
						while (iterr3.hasNext()) {
							children2.add(iterr3.nextResource().getURI());
						}
					}
					classToChildList.put(midNodeURI, children2);
				}
			}

			rootNode.add(generateTree(classToChildList, ontologyID, ""));
		} catch (Exception ex) {
			System.out.println("Exception occured whilst loading RDFS! "
					+ ex.getMessage());
			ex.printStackTrace();
		}

	}

	protected FetaOntologyTermModel parseRDFSClassElement(Model modl,
			Resource res) {

		FetaOntologyTermModel ontologyTerm = new FetaOntologyTermModel(res
				.getURI().toString());
		NodeIterator iter1 = (NodeIterator) modl.listObjectsOfProperty(res,
				RDFS.label);

		if (iter1.hasNext()) {
			String labelStr = "";
			while (iter1.hasNext()) {
				labelStr = labelStr + " "
						+ ((Literal) iter1.nextNode()).getValue().toString();
			}

			ontologyTerm.setLabel(labelStr);

		}

		NodeIterator iter2 = (NodeIterator) modl.listObjectsOfProperty(res,
				RDFS.comment);

		if (iter2.hasNext()) {
			String commentStr = "";
			while (iter2.hasNext()) {
				commentStr = commentStr + " "
						+ ((Literal) iter2.nextNode()).getValue().toString();
			}
			ontologyTerm.setDefinition(commentStr);

		}

		return ontologyTerm;
	}

	private DefaultMutableTreeNode generateTree(Map classToChildList,
			String classID, String printPrefix) {
		DefaultMutableTreeNode theNode = new DefaultMutableTreeNode();

		if (idToTermList.containsKey(classID)) {
			theNode.setUserObject(idToTermList.get(classID));
		} else {

			theNode.setUserObject(new FetaOntologyTermModel(classID));
		}
		ArrayList children = (ArrayList) classToChildList.get(classID);
		if (children != null) {
			Iterator i = children.iterator();
			if (i.hasNext()) {
				while (i.hasNext()) {
					String childClassID = (String) i.next();
					// System.out.println(printPrefix+childClassID);
					theNode.add(generateTree(classToChildList, childClassID,
							printPrefix + "*"));
				}
			}
		}
		return theNode;
	}

	public static void main(String[] args) {

		String ontURLAsString = "http://www.mygrid.org.uk:8100/feta-beta/mygrid/ontology/mygrid-services-lite.rdfs";
		try {
			URL ontoURL = new URL(ontURLAsString);
			FetaOntology fetaOnt = new FetaOntology(ontoURL);

			FetaRDFSParser2 parser = new FetaRDFSParser2();
			parser.loadRDFSDocument(fetaOnt.getAnnotationOntology(),
					"Feta-Ontoloji");
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
