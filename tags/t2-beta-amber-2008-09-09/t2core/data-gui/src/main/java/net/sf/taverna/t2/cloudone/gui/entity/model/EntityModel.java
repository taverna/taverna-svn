/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.cloudone.gui.entity.model;

/**
 * Parent type for most entities which can be added to the model
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class EntityModel {

	public static final int UNKNOWN_DEPTH = -1;

	private final EntityListModel parentModel;
	private boolean removable = true;

	public EntityModel(EntityListModel parentModel) {
		this.parentModel = parentModel;
	}

	public EntityListModel getParentModel() {
		return parentModel;
	}

	/**
	 * If an entity {@link #isRemovable()} then remove it from the
	 * {@link #parentModel}
	 */
	public void remove() {
		if (isRemovable()) {
			getParentModel().removeEntityModel(this);
		}
	}

	/**
	 * Can this model be removed (usually depends on whether it is the base
	 * model)
	 * 
	 * @return
	 */
	public boolean isRemovable() {
		return removable && getParentModel() != null;
	}

	/**
	 * Defaults to true
	 * 
	 * @param removable
	 */
	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	/**
	 * Depth is zero unless overriden in a sub-class and re-set
	 * 
	 * @return
	 */
	public int getDepth() {
		return 0;
	}

}
