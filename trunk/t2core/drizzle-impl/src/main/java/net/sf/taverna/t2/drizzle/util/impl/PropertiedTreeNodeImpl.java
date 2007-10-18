/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 * 
 */
public abstract class PropertiedTreeNodeImpl<O> implements
		PropertiedTreeNode<O> {

	private List<PropertiedTreeNode<O>> children;

	private PropertiedTreeNode<O> parent;

	/**
	 * 
	 */
	public PropertiedTreeNodeImpl() {
		children = new ArrayList<PropertiedTreeNode<O>>();
	}

	public void addChild(PropertiedTreeNode<O> child) {
		if (child == null) {
			throw new NullPointerException("child cannot be null");
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException ("child already owned");
		}
		children.add(child);
		if (child instanceof PropertiedTreeNodeImpl) {
			((PropertiedTreeNodeImpl<O>) child).setParent(this);
		}
	}

	public Set<O> getAllObjects() {
		Set<O> result = new HashSet<O> ();
		for (PropertiedTreeNode<O> child : children) {
			result.addAll (child.getAllObjects());
		}
		return result;
	}

	public PropertiedTreePropertyValueNode<O> getAncestorWithKey(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null");
		}
		PropertiedTreePropertyValueNode<O> result = null;
		for (PropertiedTreeNode<O> parent = this.getParent();
			(parent != null) && (result == null);
			parent = parent.getParent()) {
			if (parent instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<O> propertyParent =
					(PropertiedTreePropertyValueNode<O>) parent;
				PropertyKey k = propertyParent.getKey();
				if (k.equals(key)) {
					result = propertyParent;
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeNode#getChild(int)
	 */
	public final PropertiedTreeNode<O> getChild(final int index) {
		PropertiedTreeNode<O> result = null;
		try {
			result = children.get(index);
		} catch (IndexOutOfBoundsException e) {
			// reset result
			result = null;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeNode#getChildCount()
	 */
	public final int getChildCount() {
		return children.size();
	}

	public int getDepth() {
		int result = 0;
		for (PropertiedTreeNode<O> parent = this.getParent();
			parent != null; parent = parent.getParent(), result++);
		return result;
	}

	public final int getIndexOfChild(final PropertiedTreeNode<O> child) {
		if (child == null) {
			throw new NullPointerException("child cannot be null");
		}
		return children.indexOf(child);
	}

	public PropertiedTreeNode<O> getParent() {
		return this.parent;
	}

	@SuppressWarnings("unchecked")
	public PropertiedTreeNode<O>[] getPath() {
		return getPathList().toArray(new PropertiedTreeNode[0]);
	}

	public List<PropertiedTreeNode<O>> getPathList() {
		List<PropertiedTreeNode<O>> result;
		if (getParent() != null) {
			List<PropertiedTreeNode<O>> parentList =
				getParent().getPathList();
			parentList.add(this);
			result = parentList;
		}
		else {
			result = new ArrayList<PropertiedTreeNode<O>> ();
			result.add(this);
		}
		return result;
	}

	public void removeAllChildren() {
		for (PropertiedTreeNode<O> child : children) {
			if (child instanceof PropertiedTreeNodeImpl) {
				((PropertiedTreeNodeImpl<O>) child).setParent(null);
			}
		}
		children = new ArrayList<PropertiedTreeNode<O>> ();
	}

	/**
	 * @param parent
	 *            the parent to set - can be null
	 */
	protected final void setParent(PropertiedTreeNode<O> parent) {
		this.parent = parent;
	}

}