package net.sf.taverna.zaria;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;

import org.jdom.Element;

/**
 * Tree structure over a nested set of Zaria components,
 * yes, this is almost an exact duplicate of TreeNode but
 * as ZPane is a subclass of JComponent we can't have a
 * getParent method (JComponent already contains this) so,
 * annoyingly, we have to invent a duplicate interface
 * avoiding the name collisions. D'oh.
 * This interface also defines that ZTreeNode implementations
 * must be able to serialize their current state to XML
 * and restore from the same.
 * @author Tom Oinn
 */
public interface ZTreeNode {

	/**
	 * Parent ZTreeNode
	 * @return parent node or null if this is a root
	 */
	public ZTreeNode getZParent();
	
	/**
	 * Immediate children
	 * @return List<ZTreeNode> of child nodes
	 */
	public List<ZTreeNode> getZChildren();

	/**
	 * Get number of immediate children
	 * @return int count of immediate ZTreeNode children
	 */
	public int getZChildCount();
	
	/**
	 * Is this a root node?
	 * @return whether the node is a root
	 */
	public boolean isZRoot();
	
	/**
	 * Is this a leaf node?
	 * @return whether the node is a leaf
	 */
	public boolean isZLeaf();
	
	/**
	 * Build current state of this node in the
	 * form of a JDOM element
	 */
	public Element getElement();
	
	/**
	 * Set current state of this node, including
	 * construction of nested containers, from
	 * the specified JDOM Element
	 */
	public void configure(Element e);
	
	/**
	 * Set editable status on this node, implementations
	 * will recursively set the status on all children
	 */
	public void setEditable(boolean editable);
	
	/**
	 * Return a list of Action objects that can act
	 * on this ZTreeNode, implemented largely by
	 * subclasses.
	 */
	public List<Action> getActions();

	/**
	 * Return a list of JComponent items that should be
	 * added on the left hand side of the toolbar when
	 * in edit mode
	 */
	public List<Component> getToolbarComponents();
	
	/**
	 * Swap out the given child for the new one
	 * @param oldComponent the ZTreeNode to remove as a child
	 * @param newComponent the ZTreeNode to insert in its place
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent);
	
	/**
	 * Get the ZBasePane at the root of the component
	 * heirarchy or null if there isn't one (there will be
	 * for all cases where the component is visible)
	 */
	public ZBasePane getRoot();
	
	
	/**
	 * Indicates that the component is about to be discarded, and any cleaning up
	 * should be carried out here.
	 */
	public void discard();
	
}
