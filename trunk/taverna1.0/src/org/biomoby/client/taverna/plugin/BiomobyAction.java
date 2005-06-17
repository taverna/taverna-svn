/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction;

public class BiomobyAction extends AbstractProcessorAction {
    public JComponent getComponent(Processor processor) {
        // variables i need
        BiomobyProcessor theProcessor = (BiomobyProcessor) processor;
        Central central = theProcessor.getCentralWorker();
        
        // set up the root node
        String serviceName = theProcessor.getMobyService().getName();
        String description = theProcessor.getDescription();
        MobyServiceTreeNode service = new MobyServiceTreeNode(serviceName, description);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(service);
        
        // now add the child nodes containing useful information about the service
        DefaultMutableTreeNode input = new DefaultMutableTreeNode("Inputs");
        DefaultMutableTreeNode output = new DefaultMutableTreeNode("Outputs");
        rootNode.add(input);
        rootNode.add(output);
        
        // process inputs
        MobyData[] inputs = theProcessor.getMobyService().getPrimaryInputs();
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] instanceof MobyPrimaryDataSimple) {
                MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) inputs[i];
                StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
                MobyNamespace[] namespaces = simple.getNamespaces();
                for (int j = 0; j < namespaces.length; j++) {
                    sb.append(namespaces[j].getName() + " ");
                }
                if (namespaces.length == 0)
                    sb.append(" ANY ");
                MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType().getName() + "('" + simple.getName() + "')", sb.toString());
                input.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), input.getChildCount());
            } else {
                // we have a collection
                MobyPrimaryDataSet collection = (MobyPrimaryDataSet)inputs[i];
                DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode("Collection('" + collection.getName() + "')");
                input.insert(collectionNode, input.getChildCount());
                MobyPrimaryDataSimple[] simples = collection.getElements();
                for (int j = 0; j < simples.length; j++) {
                    MobyPrimaryDataSimple simple = simples[j];
                    StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
                    MobyNamespace[] namespaces = simple.getNamespaces();
                    for (int k = 0; k < namespaces.length; k++) {
                        sb.append(namespaces[k].getName() + " ");
                    }
                    if (namespaces.length == 0)
                        sb.append(" ANY ");
                    MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType().getName() + "('" + simple.getName() + "')", sb.toString());
                    collectionNode.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), collectionNode.getChildCount());
                }
                
            }
        }
        if (inputs.length == 0) {
            input.add(new DefaultMutableTreeNode(" None "));
        }
        
        // process outputs
        MobyData[] outputs = theProcessor.getMobyService().getPrimaryOutputs();
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] instanceof MobyPrimaryDataSimple) {
                MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) outputs[i];
                StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
                MobyNamespace[] namespaces = simple.getNamespaces();
                for (int j = 0; j < namespaces.length; j++) {
                    sb.append(namespaces[j].getName() + " ");
                }
                if (namespaces.length == 0)
                    sb.append(" ANY ");
                MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType().getName() + "('" + simple.getName() + "')", sb.toString());
                output.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), output.getChildCount());
            } else {
                // we have a collection
                MobyPrimaryDataSet collection = (MobyPrimaryDataSet)outputs[i];
                DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode("Collection('" + collection.getName() + "')");
                output.insert(collectionNode, output.getChildCount());
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
        if (outputs.length == 0) {
            output.add(new DefaultMutableTreeNode(" None "));
        }
        
        // finally return a tree describing the object
        JTree tree = new JTree(rootNode);
        tree.setCellRenderer(new BioMobyServiceTreeCustomRenderer());
        ToolTipManager.sharedInstance().registerComponent(tree);
        return tree;
    }

    public boolean canHandle(Processor processor) {
        return (processor instanceof BiomobyProcessor);
    }

    public String getDescription() {
        return "Moby Service Details";
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
