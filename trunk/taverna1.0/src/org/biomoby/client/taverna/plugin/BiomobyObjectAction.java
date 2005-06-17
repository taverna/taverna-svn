/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.Dimension;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.data.MobyDataInstance;
import org.biomoby.shared.data.MobyDataObject;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction;

public class BiomobyObjectAction extends AbstractProcessorAction {

    public JComponent getComponent(Processor processor) {
        // variables i need
        BiomobyObjectProcessor theProcessor = (BiomobyObjectProcessor) processor;
        Central central = theProcessor.getCentral();
        MobyDataType object = theProcessor.getMobyObject();
        MobyService template = new MobyService("dummy");
        // strip the lsid portion of the name
        String name = object.getName();
        if (name.indexOf(":") > 0) {
            name = name.substring(name.lastIndexOf(":") + 1);
        }
        // initialize a data object to pass into the service template
        MobyDataObject data = new MobyDataObject("");
        data.setDataType(new MobyDataType(name));
        data.setXmlMode(MobyDataInstance.CENTRAL_XML_MODE);

        // create the nodes for the tree
        MutableTreeNode parent = new DefaultMutableTreeNode(name);
        MutableTreeNode inputNode = new DefaultMutableTreeNode("Feeds into");
        MutableTreeNode outputNode = new DefaultMutableTreeNode("Produced by");

        // what services does this object feed into?
        template.setInputs(new MobyData[] { data });
        MobyService[] services = null;
        try {
            services = central.findService(template, null, true, false);
        } catch (MobyException e) {
            return new JTree(new String[] { "Error finding services",
                    "TODO: create a better Error" });
        }
        createTreeNodes(inputNode, services);
        if (inputNode.getChildCount() == 0)
            inputNode.insert(new DefaultMutableTreeNode("Object Doesn't Currently Feed Into Any Services"), 0);
        
        
        // what services return this object?
        template = null;
        template = new MobyService("dummy");
        template.setOutputs(new MobyData[] { data });
        services = null;
        try {
            services = central.findService(template, null, true, false);
        } catch (MobyException e) {
            return new JTree(new String[] { "Error finding services",
                    "TODO: create a better Error" });
        }
        createTreeNodes(outputNode, services);
        if (outputNode.getChildCount() == 0)
            outputNode.insert(new DefaultMutableTreeNode("Object Isn't Produced By Any Services"), 0);
        // what kind of object is this?

        // set up the nodes
        parent.insert(inputNode, 0);
        parent.insert(outputNode, 1);

        // finally return a tree describing the object
        JTree tree = new JTree(parent);
        tree.setCellRenderer(new BioMobyObjectTreeCustomRenderer());
        ToolTipManager.sharedInstance().registerComponent(tree);
        return new JScrollPane(tree);
    }

    private void createTreeNodes(MutableTreeNode parentNode, MobyService[] services) {
        HashMap inputHash;
        inputHash = new HashMap();
        for (int x = 0; x < services.length; x++) {
            DefaultMutableTreeNode authorityNode = null;
            if (!inputHash.containsKey(services[x].getAuthority())) {
                authorityNode = new DefaultMutableTreeNode(services[x].getAuthority());
            } else {
                authorityNode = (DefaultMutableTreeNode) inputHash.get(services[x].getAuthority());
            }
            MobyServiceTreeNode serv = new MobyServiceTreeNode(services[x].getName(), services[x].getDescription());
            MutableTreeNode temp = new DefaultMutableTreeNode(serv);
            DefaultMutableTreeNode objects = new DefaultMutableTreeNode("Produces");
            // add to this node the MobyObjectTreeNodes that it produces!
            MobyData[] outputs = services[x].getPrimaryOutputs();
            for (int i = 0; i < outputs.length; i++) {
                if (outputs[i] instanceof MobyPrimaryDataSimple) {
                    MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) outputs[i];
                    StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
                    MobyNamespace[] namespaces = simple.getNamespaces();
                    for (int j = 0; j < namespaces.length; j++) {
                        sb.append(namespaces[j].getName() + " ");
                    }
                    if (namespaces.length == 0)
                        sb.append("ANY ");
                    MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType().getName() + "('" + simple.getName() + "')", sb.toString());
                    objects.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), objects.getChildCount());
                } else {
                    // we have a collection
                    MobyPrimaryDataSet collection = (MobyPrimaryDataSet)outputs[i];
                    DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode("Collection('" + collection.getName() + "')");
                    objects.insert(collectionNode, objects.getChildCount());
                    MobyPrimaryDataSimple[] simples = collection.getElements();
                    for (int j = 0; j < simples.length; j++) {
                        MobyPrimaryDataSimple simple = simples[j];
                        StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
                        MobyNamespace[] namespaces = simple.getNamespaces();
                        for (int k = 0; k < namespaces.length; k++) {
                            sb.append(namespaces[k].getName() + " ");
                        }
                        if (namespaces.length == 0)
                            sb.append("ANY ");
                        MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType().getName() + "('" + simple.getName() + "')", sb.toString());
                        collectionNode.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), collectionNode.getChildCount());
                    }
                    
                }
            }
            
            temp.insert(objects, temp.getChildCount());
            
            authorityNode.insert(temp, authorityNode.getChildCount());
            inputHash.put(services[x].getAuthority(), authorityNode);

        }
        for (Iterator it = inputHash.keySet().iterator(); it.hasNext();) {
            parentNode.insert((DefaultMutableTreeNode) inputHash.get((String)it.next()), parentNode.getChildCount());
        }
    }

    public boolean canHandle(Processor processor) {
        return (processor instanceof BiomobyObjectProcessor);
    }

    public String getDescription() {
        return "Moby Object Details";
    }

    public ImageIcon getIcon() {
        Class cls = this.getClass();
        URL url = cls.getClassLoader().getResource(
                "org/biomoby/client/taverna/plugin/moby_small.gif");
        return new ImageIcon(url);
    }

    public Dimension getFrameSize() {
        return new Dimension(450, 450);
    }

}
