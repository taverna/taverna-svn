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
package net.sf.taverna.t2.cloudone.gui.entity.view;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.SingletonListModel;

import org.apache.log4j.Logger;

/**
 * A view which can only contain a {@link DataDocumentView}, {@link StringView}
 * or {@link LiteralView}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class SingletonListView extends EntityListView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4750583911881618270L;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SingletonListView.class);

	public SingletonListView(SingletonListModel singletonModel) {
		super(singletonModel, null);
	}

	@Override
	public void addEntityToModel(EntityModel entityModel) {
		super.addEntityToModel(entityModel);
		if (!getModel().getEntityModels().isEmpty()) {
			addSchemesPanel.setVisible(false);
		}
	}

	@Override
	protected void removeViewComponent(JComponent view) {
		super.removeViewComponent(view);
		addSchemesPanel.setVisible(true);
	}

	@Override
	protected void setDefaultBorder() {
		// None
	}

}
