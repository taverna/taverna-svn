/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenh�ft, INB, University of Luebeck   
 * modified 2010 Hajo Nils Krabbenh�ft, spratpix GmbH & Co. KG
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

package net.sf.taverna.t2.activities.usecase.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.usecase.KnowARCConfigurationFactory;
import net.sf.taverna.t2.activities.usecase.UseCaseActivity;
import net.sf.taverna.t2.activities.usecase.UseCaseActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import de.uni_luebeck.inb.knowarc.gui.KnowARCConfigurationDialog;

@SuppressWarnings("serial")
public class UseCaseActivityConfigureAction extends ActivityConfigurationAction<UseCaseActivity, UseCaseActivityConfigurationBean> {

	private final Frame owner;

	public UseCaseActivityConfigureAction(UseCaseActivity activity, Frame owner) {
		super(activity);
		putValue(Action.NAME, "Configure UseCase invocation");
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		new KnowARCConfigurationDialog(owner, false, KnowARCConfigurationFactory.getConfiguration()).setVisible(true);
	}
}
