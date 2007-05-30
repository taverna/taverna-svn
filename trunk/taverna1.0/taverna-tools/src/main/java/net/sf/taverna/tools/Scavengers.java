package net.sf.taverna.tools;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;


import net.sf.taverna.utils.MyGridConfiguration;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerRegistry;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


import static org.embl.ebi.escience.scufl.XScufl.XScuflNS;

public class Scavengers {
		
	public static Namespace NS = Namespace.getNamespace("http://taverna.sf.net/xml/scavengers");

	/**
	 * Get the full list of default/configured scavengers for this
	 * Taverna installation.
	 * 
	 * @see DefaultMutableTreeNode
	 * @return List<Scavenger>
	 */
	public static List<Scavenger> getScavengers() {
		List<Scavenger> locals = ScavengerRegistry.instance().getScavengers();
		List<Scavenger> scavengers = new ArrayList<Scavenger>(locals);
		
		List<ScavengerHelper> helpers = ScavengerHelperRegistry.instance().getScavengerHelpers();
		for (ScavengerHelper helper: helpers) {
			if (helper instanceof WebScavengerHelper) {
				// Constructor is too weird, we have to skip it
				continue;
			}
			for (Scavenger scavenger : helper.getDefaults()) {
				scavengers.add(scavenger);
			}
		}
		return scavengers;
	}
	
	
	/**
	 * Print an XML representation of all the installed scavengers and their
	 * factories. The XML document mirrors the tree as shown in the 
	 * Taverna workbench Scavenger tree panel.
	 * 
	 */
	public static void main(String[] args)  {
		MyGridConfiguration.loadMygridProperties();
		Element root = allScavengersAsXML();
		Document doc = new Document(root);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		try {
			xo.output(doc, System.out);
		} catch (IOException e) {
			System.err.println("Could not write XML" + e.getMessage());
		}
		System.exit(0);
	}

	/**
	 * Represent as XML all the installed scavengers and their
	 * factories.
	 * 
	 * @return An Element <code>scavengers</code>
	 */
	public static Element allScavengersAsXML() {
		List<Scavenger> scavengers = getScavengers();
		Element root = new Element("scavengers", NS);
		root.addNamespaceDeclaration(XScuflNS);
		for (Scavenger scavenger: scavengers) {
			root.addContent(scavengerAsXML(scavenger));			
		}		
		return root;
	}


	/**
	 * Represent a tree of scavengers as XML. 
	 * 
	 * @see getScavengers()
	 * @param node A DefaultMutableTreeNode as in the list of <code>getScavengers()</code>.
	 * @return An Element, namely a <code>factory</code> or <code>tree</code>
	 */
	public static Element scavengerAsXML(DefaultMutableTreeNode node) {
		Element element;
		Object nodeObject = node.getUserObject();
		//System.out.print(indention +  nodeObject.getClass() + ": ");
		if (nodeObject instanceof ProcessorFactory) {
			ProcessorFactory factory = (ProcessorFactory) nodeObject;
			element = new Element("factory", NS);
			element.setAttribute("name", factory.getName());
			Element fragment = factory.getXMLFragment();
			element.addContent(fragment);
		} else {
			element = new Element("tree", NS);
			element.setAttribute("name", nodeObject.toString());
		}
		// NOTE! Assumes there are Scavengers only!
		Enumeration children = node.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			element.addContent(scavengerAsXML(child));
		}
		return element;
	}
}
