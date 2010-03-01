/*******************************************************************************
 * Copyright (C) 2010 Hajo Nils Krabbenh�ft, spratpix GmbH & Co. KG   
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

package net.sf.taverna.t2.activities.usecase.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.usecase.UseCaseActivity;
import net.sf.taverna.t2.activities.usecase.actions.UseCaseActivityConfigureAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

/**
 * This class adds the plugin configuration action to the context menu of every use case activity.
 * 
 * @author Hajo Nils Krabbenh�ft
 */
public class ConfigureUseCaseMenuAction extends AbstractConfigureActivityMenuAction<UseCaseActivity> {

	public ConfigureUseCaseMenuAction() {
		super(UseCaseActivity.class);
	}

	@Override
	protected Action createAction() {
		UseCaseActivityConfigureAction configAction = new UseCaseActivityConfigureAction(findActivity(), getParentFrame());
		addMenuDots(configAction);
		return configAction;
	}

}
