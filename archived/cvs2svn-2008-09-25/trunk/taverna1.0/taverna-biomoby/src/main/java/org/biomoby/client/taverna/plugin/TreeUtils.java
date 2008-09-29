/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.ibm.lsid.LSID;

/**
 * This class contains some methods that are useful in creating a Datatypes tree
 * from an input stream of RDF
 * 
 * @author Edward Kawas
 * 
 */
public class TreeUtils {

    /*
     * method from MobyTree.java, but logic has changed.
     */
    private static DefaultMutableTreeNode addObject(
	    DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
	parent.add(child);
	return child;
    }

    /*
     * copied over from MobyTree.java - replaced call to RESOURCES with an http
     * call
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Household> createHomes(InputStream in) {
	HashMap<String, Household> homes = new HashMap<String, Household>(); // (key=parent,val=household)
	try {
	    // create an empty model
	    Model model = ModelFactory.createDefaultModel();

	    // read the RDF/XML data
	    model.read((in), "");
	    StmtIterator iter;
	    Statement stmt;

	    iter = model.listStatements();
	    while (iter.hasNext()) {
		stmt = (Statement) iter.next();
		String sub = stmt.getSubject().getURI();
		String obj = stmt.getObject().toString();
		if (sub != null) {
		    try {
			if (sub.indexOf("/MOBY_SUB_COMPONENT/") > 0)
			    continue;
			if (sub.indexOf("#") > 0)
			    sub = sub.substring(sub.indexOf("#") + 1, sub
				    .length());
			else if (sub.lastIndexOf("/Objects/") > 0) {
			    sub = sub.substring(sub.lastIndexOf("/Objects/")
				    + "/Objects/".length());
			}
			LSID lsid = new LSID(sub);
			sub = lsid.getObject();
		    } catch (Exception e) {
		    }
		}
		if (obj != null) {
		    if (obj.indexOf("/MOBY_SUB_COMPONENT/") > 0)
			continue;
		    if (obj.indexOf("#") > 0)
			obj = obj.substring(obj.indexOf("#") + 1, obj.length());
		    else if (obj.lastIndexOf("/Objects/") > 0) {
			obj = obj.substring(obj.lastIndexOf("/Objects/")
				+ "/Objects/".length());
		    }
		    try {
			LSID lsid = new LSID(obj);
			obj = lsid.getObject();
		    } catch (Exception e) {
		    }
		}

		if (stmt.getPredicate().getURI().indexOf("subClassOf") > 0) {
		    // System.out.println(obj);
		    // we have the relationship that we want in the tree
		    if (homes.containsKey(obj)) {
			// hash contains the home -> get the home and add child
			// to household
			Household h = homes.get(obj);
			ArrayList<String> ch = h.getChildren();
			ch.add(sub);
			h.setChildren(ch);
			homes.put(obj, h);
		    } else {
			// hash doesn't have the parent, so add the parent and
			// child to a new household
			ArrayList<String> ch = new ArrayList<String>();
			ch.add(sub); // add the child
			Household h = new Household(obj, ch);
			homes.put(obj, h);
		    }
		} // hashmap is created

	    }

	} catch (Exception e) {
	    System.err.println("Failed: " + e.getMessage() + "\n"
		    + e.getStackTrace());
	}

	return homes;
    }

    /*
     * copied over from MobyTree.java
     */
    @SuppressWarnings("unchecked")
    public static DefaultMutableTreeNode fillSubTree(
	    DefaultMutableTreeNode parentNode, ArrayList<String> children,
	    HashMap<String, Household> hashmap, String base) {
	Collections.sort(children);
	Iterator<String> it = children.iterator();
	while (it.hasNext()) {
	    String nextKid = it.next();
	    BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
		    base, "", nextKid);
	    DefaultMutableTreeNode p1 = addObject(parentNode,
		    new DefaultMutableTreeNode(f));
	    Household param = hashmap.get(nextKid);

	    if (param != null) {
		// mt.addObject(p1, nextKid);
		fillSubTree(p1, param.getChildren(), hashmap, base);
	    }

	}
	return parentNode;
    }
}
