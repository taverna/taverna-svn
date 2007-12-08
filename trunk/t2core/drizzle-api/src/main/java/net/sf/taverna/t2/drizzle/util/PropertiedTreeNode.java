/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.List;
import java.util.Set;

/**
 * @author alanrw
 * 
 * @param <O>
 *            The class of object within the PropertiedObjectSet of the tree
 *            model of which the node is a part.
 */
public interface PropertiedTreeNode<O> {

	/**
	 * Return the child of the node at the specified index. null is returned if
	 * the index is not valid for the node.
	 * 
	 * @param index
	 * @return
	 */
	PropertiedTreeNode<O> getChild(final int index);

	/**
	 * Return the number of children of the node.
	 * 
	 * @return
	 */
	int getChildCount();
	
	int getActualChildCount();

	/**
	 * Return the index of the child node within the children of the current
	 * node. -1 is returned if the specified child is not actually a child of
	 * the current node.
	 * 
	 * @param child
	 * @return
	 */
	int getIndexOfChild(final PropertiedTreeNode<O> child);

	/**
	 * Add the child node to the end of the list of children of the current
	 * node.
	 * 
	 * @param child
	 */
	void addChild(final PropertiedTreeNode<O> child);

	/**
	 * Return the parent, if any, of the current node. null is returned if the
	 * node is a root.
	 * 
	 * @return
	 */
	PropertiedTreeNode<O> getParent();

	/**
	 * Return the ancestor of the current node that has the specified
	 * PropertyKey. null is returned if there is no such ancestor.
	 * 
	 * @param key
	 * @return
	 */
	PropertiedTreePropertyValueNode<O> getAncestorWithKey(final PropertyKey key);

	/**
	 * Return the set of all objects that are represented by
	 * PropertiedTreeObjectNodes that are descendants of the current node.
	 * 
	 * @return
	 */
	Set<O> getAllObjects();

	/**
	 * Empty the list of children.
	 */
	void removeAllChildren();

	/**
	 * Return the number of ancestors of the current node. For a root, zero is
	 * returned.
	 * 
	 * @return
	 */
	int getDepth();

	/**
	 * Return a list of the ancestors, starting from the root, of the current
	 * node. The current node is the last entry in the list.
	 * 
	 * @return
	 */
	List<PropertiedTreeNode<O>> getPathList();

	/**
	 * Return an array containing the list of ancestors, starting from the root,
	 * of the current node. The current node is the last element of the array.
	 * 
	 * @return
	 */
	PropertiedTreeNode<O>[] getPath();

}
