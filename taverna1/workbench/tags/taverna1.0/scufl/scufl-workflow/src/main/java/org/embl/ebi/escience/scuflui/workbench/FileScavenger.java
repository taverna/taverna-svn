/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowScavenger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * A scavenger which traverses the filesystem from the specified point looking
 * for workflow definitions and creating corresponding workflow scavengers if it
 * finds any
 * 
 * @author Tom Oinn
 */
public class FileScavenger extends Scavenger {

	/**
	 * Create a new file scavenger starting at the specified File
	 */
	public FileScavenger(File file) throws ScavengerCreationException {
		super("File traversal from : " + file.toString());
		DefaultMutableTreeNode node = getNode(file);
		if (node != null) {
			add(node);
		} else {
			add(new DefaultMutableTreeNode(
					"<font color=\"red\">No workflows found here!</font>"));
		}
	}

	private DefaultMutableTreeNode getNode(File f) {
		if (f.isDirectory()) {
			// Directory, create a new textual node and recurse
			File[] children = f.listFiles();
			if (children.length > 0) {
				DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(f
						.getName());
				List<DefaultMutableTreeNode> childDirectoryNodes = new ArrayList<DefaultMutableTreeNode>();
				boolean hasContents = false;
				for (int i = 0; i < children.length; i++) {
					DefaultMutableTreeNode child = getNode(children[i]);
					if (child != null) {
						if (child.getUserObject() instanceof ProcessorFactory) {
							dirNode.add(child);
							hasContents = true;
						} else {
							childDirectoryNodes.add(child);
						}
					}
				}
				// Added all workflows, now add all subdirectories
				for (DefaultMutableTreeNode childNode : childDirectoryNodes) {
					dirNode.add(childNode);
					hasContents = true;
				}
				if (hasContents) {
					return dirNode;
				} else {
					return null;
				}
			} else {
				// Empty directory, ignore it
				return null;
			}
		} else {
			// File, if it's an XML file then examine it and load
			// workflow if possible
			String name = f.getName();
			if (name.endsWith(".xml")) {
				try {
					InputStreamReader isr = new InputStreamReader(f.toURL()
							.openStream());
					SAXBuilder builder = new SAXBuilder(false);
					Document doc = builder.build(isr);
					return new WorkflowScavenger(doc, name);
				} catch (Exception ex) {
					return null;
				}
			} else {
				return null;
			}
		}
	}

}
