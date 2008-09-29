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

import java.util.List;

import org.apache.log4j.Logger;

/**
 * Represents the root of a model which contains only a {@link StringModel},
 * {@link DataDocumentModel} or {@link LiteralModel}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class SingletonListModel extends EntityListModel {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SingletonListModel.class);

	public SingletonListModel(int depthOfSingleton) {
		super(null);
		setDepth(depthOfSingleton + 1);
	}
/**
 * Can only have one {@link EntityModel}
 */
	@Override
	public void addEntityModel(EntityModel entityModel) {
		synchronized (this) {
			if (!getEntityModels().isEmpty()) {
				throw new IllegalStateException(
						"Can't add more than one entity model");
			}
			super.addEntityModel(entityModel);
		}
	}

	public EntityModel getSingleton() {
		List<EntityModel> children = getEntityModels();
		if (children.isEmpty()) {
			return null;
		}
		return children.get(0);
	}

}
