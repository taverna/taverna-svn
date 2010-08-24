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
package net.sf.taverna.t2.activities.sadi.views;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.activities.sadi.RestrictionNode;


/**
 * 
 *
 * @author David Withers
 */
public class SADITreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 1L;

	private RestrictionNode restrictedProperty;

	private AbstractButton button;
	
	/**
	 * Constructs a new SADITreeNode.
	 */
	public SADITreeNode() {
	}

	/**
	 * Constructs a new SADITreeNode.
	 * 
	 * @param userObject
	 */
	public SADITreeNode(Object userObject) {
		super(userObject);
	}

	/**
	 * Constructs a new SADITreeNode.
	 * @param restrictedProperty
	 * @param button
	 */
	public SADITreeNode(RestrictionNode restrictedProperty, AbstractButton button) {
		super();
		this.restrictedProperty = restrictedProperty;
		this.button = button;
	}

	/**
	 * Returns the button.
	 *
	 * @return the button
	 */
	public AbstractButton getButton() {
		return button;
	}

	/**
	 * Sets the value of button.
	 *
	 * @param button the new value for button
	 */
	public void setButton(AbstractButton button) {
		this.button = button;
	}

	/**
	 * Returns the restrictedProperty.
	 *
	 * @return the restrictedProperty
	 */
	public RestrictionNode getRestrictedProperty() {
		return restrictedProperty;
	}

	/**
	 * Sets the value of restrictedProperty.
	 *
	 * @param restrictedProperty the new value for restrictedProperty
	 */
	public void setRestrictedProperty(RestrictionNode restrictedProperty) {
		this.restrictedProperty = restrictedProperty;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getParent()
	 */
	@Override
	public SADITreeNode getParent() {
		return (SADITreeNode) super.getParent();
	}

	public List<SADITreeNode> getChildren() {
		List<SADITreeNode> children = new ArrayList<SADITreeNode>();
		Enumeration<?> enumeration = children();
		while (enumeration.hasMoreElements()) {
			children.add((SADITreeNode) enumeration.nextElement());
		}
		return children;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getPath()
	 */
	@Override
	public SADITreeNode[] getPath() {
		TreeNode[] path = super.getPath();
		SADITreeNode[] sadiPath = new SADITreeNode[path.length];
		for (int i = 0; i < path.length; i++) {
			sadiPath[i] = (SADITreeNode) path[i];
		}
		return sadiPath;
	}

}
