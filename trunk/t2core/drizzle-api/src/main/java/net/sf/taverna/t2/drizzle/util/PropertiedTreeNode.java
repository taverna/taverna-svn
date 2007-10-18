/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.List;
import java.util.Set;

/**
 * @author alanrw
 *
 */
public interface PropertiedTreeNode<O> {

	PropertiedTreeNode<O> getChild(final int index);
	
	int getChildCount();
	
	int getIndexOfChild(final PropertiedTreeNode<O> child);
	
	void addChild(final PropertiedTreeNode<O> child);
	
	PropertiedTreeNode<O> getParent();
	
	PropertiedTreePropertyValueNode<O> getAncestorWithKey(final PropertyKey key);
	
	Set<O> getAllObjects();
	
	void removeAllChildren();
	
	int getDepth();
	
	List<PropertiedTreeNode<O>> getPathList();
	
	PropertiedTreeNode<O>[] getPath();
}
