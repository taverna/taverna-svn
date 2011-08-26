/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.beans.IntrospectionException;
import org.jdom.*;
import java.util.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.baclava.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * Defines the manner in which iterators should be constructed for a given
 * processor instance.
 * 
 * @author Tom Oinn
 */
public class IterationStrategy {

	static final Namespace NS = Namespace.getNamespace("i",
			"http://org.embl.ebi.escience/xscufliteration/0.1beta10");

	StrategyModel strategyModel = null;

	public TreeModel getTreeModel() {
		return this.strategyModel;
	}

	/**
	 * Create a new IterationStrategy with the default iteration strategy for
	 * the supplied processor
	 */
	public IterationStrategy(Processor p) {
		strategyModel = new StrategyModel(new CrossNode());
		InputPort[] inputs = p.getBoundInputPorts();
		for (int i = 0; i < inputs.length; i++) {
			strategyModel.insertNodeInto(new LeafNode(inputs[i].getName()),
					(MutableTreeNode) strategyModel.getRoot(), i);
		}
		normalize();
	}

	/**
	 * Normalize the strategy model
	 */
	public void normalize() {
		strategyModel.normalize();
	}

	/**
	 * Create a new IterationStrategy from the supplied JDOM Element object
	 */
	public IterationStrategy(Element strategyElement) {
		MutableTreeNode rootNode = nodeForElement((Element) strategyElement
				.getChildren().get(0));
		strategyModel = new StrategyModel(rootNode);
		normalize();
	}

	private MutableTreeNode nodeForElement(Element element) {
		String elementName = element.getName();
		if (elementName.equals("iterator")) {
			// Found a leaf node
			return new LeafNode(element.getAttributeValue("name"));
		} else {
			MutableTreeNode combination = null;
			if (elementName.equals("dot")) {
				combination = new DotNode();
			} else {
				combination = new CrossNode();
			}
			for (Iterator i = element.getChildren().iterator(); i.hasNext();) {
				combination.insert(nodeForElement((Element) i.next()), 0);
			}
			return combination;
		}
	}

	/**
	 * Write out the current iteration strategy as an Element
	 */
	public Element getElement() {
		Element rootElement = new Element("iterationstrategy", XScufl.XScuflNS);
		rootElement.addContent(elementForNode((MutableTreeNode) strategyModel
				.getRoot()));
		return rootElement;
	}

	private Element elementForNode(MutableTreeNode node) {
		if (node instanceof LeafNode) {
			Element leafElement = new Element("iterator", NS);
			leafElement.setAttribute("name", (String) (((LeafNode) node)
					.getUserObject()));
			return leafElement;
		} else {
			Element combination = null;
			if (node instanceof DotNode) {
				combination = new Element("dot", NS);
			} else {
				combination = new Element("cross", NS);
			}
			for (Enumeration en = node.children(); en.hasMoreElements();) {
				combination.addContent(elementForNode((MutableTreeNode) en
						.nextElement()));
			}
			return combination;
		}
	}

	/**
	 * Construct a concrete instance of the iteration tree from this strategy
	 * object and the Map of port name -> BaclavaIteratorNode objects.
	 * 
	 * @throws IntrospectionException
	 *             if a leaf iterator is named in the strategy but doesn't occur
	 *             in the map
	 */
	public ResumableIterator buildIterator(Map iteratorNodes)
			throws IntrospectionException {
		return iteratorForNode((MutableTreeNode) strategyModel.getRoot(),
				iteratorNodes);
	}

	private ResumableIterator iteratorForNode(MutableTreeNode node,
			Map iteratorNodes) throws IntrospectionException {
		if (node instanceof LeafNode) {
			String iteratorName = (String) ((LeafNode) node).getUserObject();
			BaclavaIteratorNode iteratorNode = (BaclavaIteratorNode) iteratorNodes
					.get(iteratorName);
			if (iteratorNode == null) {
				throw new IntrospectionException(
						"Unable to bind leaf iterator with name '"
								+ iteratorName + "'");
			}
			return iteratorNode;
		} else {
			DefaultMutableTreeNode combination = null;
			if (node instanceof DotNode) {
				combination = new LockStepIteratorNode();
			} else {
				combination = new JoinIteratorNode();
			}
			for (Enumeration en = node.children(); en.hasMoreElements();) {
				combination.add((MutableTreeNode) iteratorForNode(
						(MutableTreeNode) en.nextElement(), iteratorNodes));
			}
			return (ResumableIterator) combination;
		}
	}

}

class StrategyModel extends DefaultTreeModel {
	public StrategyModel() {
		this(new CrossNode());
	}

	public StrategyModel(MutableTreeNode node) {
		super(node);
		addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				//
			}

			public void treeNodesInserted(TreeModelEvent e) {
				//
			}

			public void treeNodesRemoved(TreeModelEvent e) {
				/**
				 * // If a node is removed, all children that are // instances
				 * of BaclavaIterator should be re-attached // to the root node
				 * to prevent them getting lost DefaultMutableTreeNode n =
				 * (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());
				 * if (n instanceof LeafNode == false) { Enumeration en =
				 * (n.depthFirstEnumeration()); Set nodesToRescue = new
				 * HashSet(); while (en.hasMoreElements()) {
				 * DefaultMutableTreeNode m =
				 * (DefaultMutableTreeNode)en.nextElement(); if (m instanceof
				 * LeafNode) { nodesToRescue.add(m); } } for (Iterator i =
				 * nodesToRescue.iterator(); i.hasNext();) {
				 * DefaultMutableTreeNode m = (DefaultMutableTreeNode)i.next();
				 * StrategyModel.this.removeNodeFromParent(m);
				 * StrategyModel.this.insertNodeInto(m,(MutableTreeNode)StrategyModel.this.getRoot(),0); } }
				 */
			}

			public void treeStructureChanged(TreeModelEvent e) {
				//
			}
		});
	}

	/**
	 * Performs transformations on the tree, removing redundant nodes.
	 */
	public synchronized void normalize() {
		boolean finished = false;
		while (!finished) {
			finished = true;
			Enumeration e = ((DefaultMutableTreeNode) getRoot())
					.breadthFirstEnumeration();
			while (e.hasMoreElements() && finished == true) {
				MutableTreeNode n = (MutableTreeNode) e.nextElement();
				// Check whether anything needs doing

				// Check for collation nodes with no children
				if (!(n instanceof LeafNode) && n.getParent() != null
						&& n.getChildCount() == 0) {
					// Remove the node from its parent and set finished to false
					removeNodeFromParent(n);
					finished = false;
				} else if (!(n.isLeaf()) && n.getParent() != null
						&& n.getChildCount() == 1) {
					// Is a collation node with a single child, and therefore
					// pointless.
					// Replace it with the child node
					MutableTreeNode child = (MutableTreeNode) n.getChildAt(0);
					MutableTreeNode parent = (MutableTreeNode) n.getParent();
					// Find the index of the collation node in its parent
					int oldIndex = getIndexOfChild(n.getParent(), n);
					removeNodeFromParent(n);
					insertNodeInto(child, parent, oldIndex);
					finished = false;
				} else if (n.getParent() == null && n.getChildCount() == 1) {
					// Is the root node but with only one child, so must
					// be a collation node and have no effect on the iterator
					MutableTreeNode child = (MutableTreeNode) n.getChildAt(0);
					removeNodeFromParent(child);
					setRoot(child);
					finished = false;
				}
			}
		}
	}
}
