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
package net.sf.taverna.workbench.ui.beanshell.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MimeTypeConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class BeanshellViewer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		JFrame frame = new JFrame();
		
		BeanshellActivity beanshellActivity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		try {
			beanshellActivity.configure(bean);
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ContextualViewFactory viewFactoryForBeanType = ContextualViewFactoryRegistry.getInstance().getViewFactoryForObject(beanshellActivity);
		assertNotNull("The beanshsell view factory should not be null",viewFactoryForBeanType);
		assertTrue("Was not a  Beanshell view factory", viewFactoryForBeanType instanceof BeanshellActivityViewFactory);
		ContextualView viewType = viewFactoryForBeanType.getView(beanshellActivity);
		JButton button = new JButton();
		button.addActionListener(viewType.getConfigureAction(null));
		frame.add(viewType);
		frame.add(button);
		frame.setVisible(true);
		
		
		JFrame mimeFrame = new JFrame();
		MimeTypeConfig mimeConf = new MimeTypeConfig();
		mimeFrame.add(mimeConf);
		mimeFrame.setTitle("mime");
		mimeFrame.setVisible(true);
		
		
		
	}

}
