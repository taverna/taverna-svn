/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

/**
 * 
 *
 * @author David Withers
 */
public class SADIConfigurationPanel extends ActivityConfigurationPanel<SADIActivity, SADIActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;

	private SADIActivity activity;
	
	private JTextField exampleValueField;

	private SADIActivityConfigurationBean configuration;

	public SADIConfigurationPanel(SADIActivity activity) {
		this.activity = activity;
		initialise();
	}

	private void initialise() {
		configuration = activity.getConfiguration();

//		exampleValueField = new JTextField(String.valueOf(configuration.getExampleValue()));
		add(new JLabel("Value: "));
		add(exampleValueField);

		this.validate();
	}
	
	@Override
	public SADIActivityConfigurationBean getConfiguration() {
		return configuration;
	}

	@Override
	public boolean isConfigurationChanged() {
//		return !configuration.getExampleValue().equals(exampleValueField.getText());
		return false;
	}

	@Override
	public void noteConfiguration() {
		SADIActivityConfigurationBean newConfiguration = new SADIActivityConfigurationBean();
//		newConfiguration.setExampleValue(exampleValueField.getText());
		configuration = newConfiguration;
	}

	@Override
	public void refreshConfiguration() {
		removeAll();
		initialise();
	}

	@Override
	public boolean checkValues() {
		// TODO Not yet done
		return true;
	}
	
}
