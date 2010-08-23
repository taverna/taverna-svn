/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import ca.wilkinsonlab.sadi.utils.OwlUtils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * A node in a tree of restrictions on OWL properties.
 * 
 * @author David Withers
 */
public class RestrictionNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	private OntProperty ontProperty;
	private OntClass ontClass;
	private Map<String, List<?>> values;
	private boolean exclusive, selected;

	/**
	 * Constructs a new non-exclusive RestrictionNode with no property.
	 * 
	 * @param ontClass
	 *            the restriction class - must not be null
	 */
	public RestrictionNode(OntClass ontClass) {
		this(null, ontClass);
	}

	/**
	 * Constructs a new non-exclusive RestrictionNode.
	 * 
	 * @param ontProperty
	 *            the restricted property
	 * @param ontClass
	 *            the restriction class - must not be null
	 */
	public RestrictionNode(OntProperty ontProperty, OntClass ontClass) {
		this(ontProperty, ontClass, false);
	}

	/**
	 * Constructs a new RestrictionNode.
	 * 
	 * @param ontProperty
	 *            the restricted property
	 * @param ontClass
	 *            the restriction class - must not be null
	 * @param exclusive
	 *            whether the restriction is exclusive
	 */
	public RestrictionNode(OntProperty ontProperty, OntClass ontClass, boolean exclusive) {
		if (ontClass == null) {
			throw new IllegalArgumentException("ontClass nust not be null");
		}
		this.ontProperty = ontProperty;
		this.ontClass = ontClass;
		this.exclusive = exclusive;
		values = new HashMap<String, List<?>>();
	}

	/**
	 * Returns the ontProperty.
	 * 
	 * @return the ontProperty
	 */
	public OntProperty getOntProperty() {
		return ontProperty;
	}

	/**
	 * Returns the ontClass.
	 * 
	 * @return the ontClass
	 */
	public OntClass getOntClass() {
		return ontClass;
	}

	/**
	 * Returns <code>true</code> if this node is exclusive.
	 * 
	 * @return <code>true</code> if this node is exclusive
	 */
	public boolean isExclusive() {
		return exclusive;
	}

	/**
	 * Returns <code>true</code> if this node is selected.
	 *
	 * @return <code>true</code> if this node is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets this node and its parent node as selected.
	 */
	public void setSelected() {
		selected = true;
		TreeNode parentNode = getParent();
		if (parentNode instanceof RestrictionNode) {
			RestrictionNode restrictionNode = (RestrictionNode) parentNode;
			restrictionNode.setSelected();
		}
	}

	/**
	 * Sets this node and its children as unselected.
	 */
	public void clearSelected() {
		selected = false;
		for (RestrictionNode restrictionNode : getChildren()) {
			restrictionNode.clearSelected();
		}
	}
	
	/**
	 * Returns the values for the specified id.
	 * 
	 * @param id
	 * @return the values
	 */
	public List<?> getValues(String id) {
		return values.get(id);
	}

	/**
	 * Sets the values for the specified id.
	 * 
	 * @param id
	 * @param values
	 *            the new values
	 */
	public void setValues(String id, List<?> values) {
		this.values.put(id, values);
	}

	/**
	 * Clears the values for the specified id.
	 * 
	 * @param id
	 */
	public void clearValues(String id) {
		values.remove(id);
	}
	
	/**
	 * Returns all the child nodes of this node that are RestrictionNodes. If
	 * there are none an empty list is returned.
	 * 
	 * @return all the child nodes of this node that are RestrictionNodes
	 */
	public List<RestrictionNode> getChildren() {
		List<RestrictionNode> children = new ArrayList<RestrictionNode>();
		Enumeration<?> enumeration = children();
		while (enumeration.hasMoreElements()) {
			Object element = enumeration.nextElement();
			if (element instanceof RestrictionNode) {
				children.add((RestrictionNode) element);
			}
		}
		return children;
	}

	/**
	 * Returns a String representation of this {@link RestrictionNode} formatted
	 * as 'propertyName (className)'. If the node has no property then the name
	 * of the ontClass is returned.
	 * 
	 * @return a String representation of this {@link RestrictionNode} formatted
	 *         as 'propertyName (className)'
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (ontProperty == null) {
			sb.append(OwlUtils.getLabel(ontClass));
		} else {
			sb.append(OwlUtils.getLabel(ontProperty));
			sb.append(" (");
			sb.append(OwlUtils.getLabel(ontClass));
			sb.append(')');
		}
		return sb.toString();
	}

}
