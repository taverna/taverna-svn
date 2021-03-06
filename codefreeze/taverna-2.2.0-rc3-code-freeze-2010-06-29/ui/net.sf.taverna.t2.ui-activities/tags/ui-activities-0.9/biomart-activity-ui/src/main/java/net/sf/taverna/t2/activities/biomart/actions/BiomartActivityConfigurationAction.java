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
package net.sf.taverna.t2.activities.biomart.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

import org.jdom.Element;

public class BiomartActivityConfigurationAction extends ActivityConfigurationAction<BiomartActivity, Element> {

	private static final long serialVersionUID = 3782223454010961660L;
	private final Frame owner;

	public BiomartActivityConfigurationAction(BiomartActivity activity,Frame owner) {
		super(activity);
		this.owner = owner;
	}

	@SuppressWarnings("serial")
	public void actionPerformed(ActionEvent action) {
		final BiomartConfigurationPanel configurationPanel = new BiomartConfigurationPanel(getActivity().getConfiguration());
		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner, "Biomart Activity Configuration", true, null);
		
		Action okAction = new AbstractAction("OK") {

			public void actionPerformed(ActionEvent arg0) {
				Element query = configurationPanel.getQuery();
				configureActivity(query);
				dialog.setVisible(false);
			}
			
		};
		Action cancelAction = new AbstractAction("Cancel") {

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
			
		};

		configurationPanel.setOkAction(okAction);
		configurationPanel.setCancelAction(cancelAction);
		
		dialog.getContentPane().add(configurationPanel);
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	} 

}
