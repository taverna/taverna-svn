/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biomoby.client.ui.graphical.applets.shared.Household;
import org.biomoby.shared.MobyException;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * A Scavenger that knows how to get all the Biomoby objects from a
 * specified Biomoby RESOURCES script. <p>
 * @deprecated
 */
public class BiomobyObjectScavenger extends Scavenger {

    private static final long serialVersionUID = 3545233648191289400L;
    private String defaultResourceURL = "http://biomoby.org/RESOURCES/MOBY-S/Objects";
    /**
     * Create a new Biomoby scavenger, the base parameter should
     * be the base URL of the Biomoby Object RDF document.
     */
    public BiomobyObjectScavenger(String base) throws ScavengerCreationException {
        super("Biomoby Objects @ " + base);
        
        DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode("MOBY Objects");
        insert(objectRootNode, 0);
        //TODO create a tree of Objects using RDF - PREFERED METHOD
        // Only works for mobycentral, since mobycentral currently serves this RDF
        // Would like to make this take in a url of where to retrieve the RDF
        String string = "Object";
        BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(base, "",string);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(f);
        objectRootNode.add(root);
        HashMap hashmap = null;
        try {
            hashmap = createHomes(defaultResourceURL);
        } catch (MobyException e) {
            throw new ScavengerCreationException("Error creating BioMoby Object Scavenger.");
        }
        fillSubTree(root, ((Household)hashmap.get("Object")).getChildren(), hashmap,base);
        
    }
    
    /*
     * copied over from MobyTree.java - replaced call to RESOURCES with an http call
     */
    private HashMap createHomes(String url) throws MobyException {
        HashMap homes = new HashMap(); // (key=parent,val=household)
        try {
            // create an empty model
            Model model = new ModelMem();

            InputStream in = (new URL(url).openStream());

            // read the RDF/XML data
            model.read((in), "");
            StmtIterator iter;
            Statement stmt;

            iter = model.listStatements();
            while (iter.hasNext()) {
                stmt = (Statement) iter.next();
                
                String sub = stmt.getSubject().getURI();
                String obj = stmt.getObject().toString();
                if (sub != null)
                    sub = sub.substring(sub.indexOf("#") + 1, sub.length());
                if (obj!= null)
                    obj = obj.substring(obj.indexOf("#") + 1, obj.length());
                
                if (stmt.getPredicate().getURI().indexOf("subClassOf") > 0) {
                    //System.out.println(obj);
                    // we have the relationship that we want in the tree
                    if (homes.containsKey(obj)) {
                        // hash contains the home -> get the home and add child to household
                        Household h = (Household)homes.get(obj);
                        ArrayList ch = h.getChildren();
                        ch.add(sub);
                        h.setChildren(ch);
                        homes.put(obj,h);
                    } else {
                        // hash doesn't have the parent, so add the parent and child to a new household
                        ArrayList ch = new ArrayList();
                        ch.add(sub); // add the child
                        Household h = new Household(obj, ch);
                        homes.put(obj,h);
                    }
                } // hashmap is created

            }

        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage()+"\n" + e.getStackTrace());
            throw new MobyException("Could not create Object Tree\r\n" + e);
        }

        return homes;
    }
    
    /*
     * copied over from MobyTree.java
     */
    private DefaultMutableTreeNode fillSubTree(DefaultMutableTreeNode parentNode, ArrayList children, HashMap hashmap,String base) {
        Collections.sort(children);
        Iterator it = children.iterator();
        while (it.hasNext()) {
            String nextKid = (String) it.next();
            BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(base, "",nextKid);
            DefaultMutableTreeNode p1 = addObject(parentNode, new DefaultMutableTreeNode(f));
            Household param = (Household) hashmap.get(nextKid);

            if (param != null) {
                //mt.addObject(p1, nextKid);
                fillSubTree(p1, param.getChildren(),hashmap,base);
            }

        }
        return parentNode;
    }
    
    /*
     * method from MobyTree.java, but logic has changed.
     */
    private DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        parent.add(child);
        return child;
    }
}
