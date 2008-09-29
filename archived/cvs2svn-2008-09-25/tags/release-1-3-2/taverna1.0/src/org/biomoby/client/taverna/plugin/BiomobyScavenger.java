/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biomoby.client.CentralImpl;
import org.biomoby.client.ui.graphical.applets.shared.Household;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.ibm.lsid.LSID;

/**
 * A Scavenger that knows how to get all the Biomoby services from a specified
 * Biomoby Central Registry. <p>
 * 
 * @version $Id: BiomobyScavenger.java,v 1.5 2006-03-16 14:51:58 edwardkawas Exp $
 * @author Martin Senger
 */
public class BiomobyScavenger extends Scavenger {

    private static final long serialVersionUID = 3545233648191289400L;

    private String defaultResourceURL = null; // use MOBY api call instead

    /**
     * Create a new Biomoby scavenger, the base parameter should be the base URL
     * of the Biomoby Central Registry.
     */
    protected BiomobyScavenger(String base, String resourceURL)
            throws ScavengerCreationException {
        super("Biomoby @ " + base);

        // get list of services and their authorities
        try {
            Central worker = new CentralImpl(base);
            Map names = worker.getServiceNamesByAuthority();
            
            ArrayList list = new ArrayList(names.keySet());
            Collections.sort(list);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                String authorityName = (String) iterator.next();
                String[] authorities = (String[]) names.get(authorityName);
                DefaultMutableTreeNode authority = new DefaultMutableTreeNode(
                        authorityName);
                add(authority);
                for (int i = 0; i < authorities.length; i++) {
                    String serviceName = (String) authorities[i];
                    BiomobyProcessorFactory f = new BiomobyProcessorFactory(
                            base, authorityName, serviceName);
                    authority.add(new DefaultMutableTreeNode(f));
                }
            }

        } catch (Exception e) {
            ScavengerCreationException sce = new ScavengerCreationException(e
                    .getMessage());
            sce.initCause(e);
            throw sce;
        }
        try {
            DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode(
                    "MOBY Objects");
            insert(objectRootNode, 0);
            String string = "Object";
            BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
                    base, "", string);
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(f);
            objectRootNode.add(root);
            HashMap hashmap = createHomes(resourceURL);
            fillSubTree(root,
                    ((Household) hashmap.get("Object")).getChildren(), hashmap,
                    base);
        } catch (Exception e) {
            ScavengerCreationException sce = new ScavengerCreationException(
                    "Could not retrieve and or process RDF document for BioMoby Objects");
            sce.initCause(e);
            throw sce;
        }

    }

    /**
     * constructor that taverna's init uses
     */
    public BiomobyScavenger(String base) throws ScavengerCreationException {
        super("Biomoby @ " + base);

        // get list of services and their authorities
        try {
            Central worker = new CentralImpl(base);
            Map names = worker.getServiceNamesByAuthority();

            Hashtable byAuthority = new Hashtable();
            for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String authorityName = (String) entry.getKey();
                String[] serviceName = (String[]) entry.getValue();
                Vector services;
                if (byAuthority.containsKey(authorityName))
                    services = (Vector) byAuthority.get(authorityName);
                else
                    services = new Vector();
                for (int i = 0; i < serviceName.length; i++) {
                    services.addElement(serviceName[i]);    
                }
                byAuthority.put(authorityName, services);
            }

            ArrayList list = new ArrayList(names.keySet());
            Collections.sort(list);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                String authorityName = (String) iterator.next();
                String[] authorities = (String[]) names.get(authorityName);
                DefaultMutableTreeNode authority = new DefaultMutableTreeNode(
                        authorityName);
                add(authority);
                for (int i = 0; i < authorities.length; i++) {
                    String serviceName = (String) authorities[i];
                    BiomobyProcessorFactory f = new BiomobyProcessorFactory(
                            base, authorityName, serviceName);
                    authority.add(new DefaultMutableTreeNode(f));
                }
            }

        } catch (Exception e) {
            ScavengerCreationException sce = new ScavengerCreationException(e
                    .getMessage());
            sce.initCause(e);
            throw sce;
        }

        try {
            
            DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode(
                    "MOBY Objects");
            insert(objectRootNode, 0);
            BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
                    base, "", "Object");
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(f);
            objectRootNode.add(root);
            // get the Moby Object rdf location from mobycentral via api call
            try {
                Central central = new CentralImpl(
                        base);
                MobyResourceRef mrr[] = central
                        .getResourceRefs();
                defaultResourceURL = null;
                for (int x = 0; x < mrr.length; x++) {
                    MobyResourceRef ref = mrr[x];
                    if (!ref.getResourceName().equals("Object"))
                        continue;
                    defaultResourceURL = ref.getResourceLocation()
                            .toExternalForm();
                    break;
                }

                if (defaultResourceURL == null)
                    throw new MobyException(
                            "Could not retrieve the location of the Moby Datatype RDF Document from the given endpoint "
                                    + base);
            } catch (MobyException e) {}
            
            HashMap hashmap = createHomes(defaultResourceURL);
            fillSubTree(root,
                    ((Household) hashmap.get("Object")).getChildren(), hashmap,
                    base);
        } catch (Exception e) {
            ScavengerCreationException sce = new ScavengerCreationException(
                    "Could not retrieve and or process RDF document for BioMoby Objects");
            sce.initCause(e);
            throw sce;
        }

    }

    /*
     * copied over from MobyTree.java - replaced call to RESOURCES with an http
     * call
     */
    private HashMap createHomes(String url) {
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
                if (sub != null) {
					try {
						if (sub.indexOf("#") > 0)
							sub = sub.substring(sub.indexOf("#") + 1, sub.length());
						LSID lsid = new LSID(sub);
						sub = lsid.getObject();
					} catch (Exception e) {
					}
				}
				if (obj!= null) {
					if (obj.indexOf("#") > 0)
						obj = obj.substring(obj.indexOf("#") + 1, obj.length());
					try {
						LSID lsid = new LSID(obj);
						obj = lsid.getObject();
					} catch (Exception e) {
					}
				}

                if (stmt.getPredicate().getURI().indexOf("subClassOf") > 0) {
                    //System.out.println(obj);
                    // we have the relationship that we want in the tree
                    if (homes.containsKey(obj)) {
                        // hash contains the home -> get the home and add child
                        // to household
                        Household h = (Household) homes.get(obj);
                        ArrayList ch = h.getChildren();
                        ch.add(sub);
                        h.setChildren(ch);
                        homes.put(obj, h);
                    } else {
                        // hash doesn't have the parent, so add the parent and
                        // child to a new household
                        ArrayList ch = new ArrayList();
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
    private DefaultMutableTreeNode fillSubTree(
            DefaultMutableTreeNode parentNode, ArrayList children,
            HashMap hashmap, String base) {
        Collections.sort(children);
        Iterator it = children.iterator();
        while (it.hasNext()) {
            String nextKid = (String) it.next();
            BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
                    base, "", nextKid);
            DefaultMutableTreeNode p1 = addObject(parentNode,
                    new DefaultMutableTreeNode(f));
            Household param = (Household) hashmap.get(nextKid);

            if (param != null) {
                //mt.addObject(p1, nextKid);
                fillSubTree(p1, param.getChildren(), hashmap, base);
            }

        }
        return parentNode;
    }

    /*
     * method from MobyTree.java, but logic has changed.
     */
    private DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
            DefaultMutableTreeNode child) {
        parent.add(child);
        return child;
    }
}
