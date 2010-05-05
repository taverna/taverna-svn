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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.actions.SADIActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestSADIActivityContextualView {

	SADIActivity activity;

	@Before
	public void setup() throws Exception {
//		activity = new SADIActivity();		
//		activity.configure(new SADIActivityConfigurationBean());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testDisovery() throws Exception {
		List<ContextualViewFactory> viewFactoriesForBeanType = ContextualViewFactoryRegistry.getInstance().getViewFactoriesForObject(activity);
		assertTrue("The view factory should not be empty", !viewFactoriesForBeanType.isEmpty());
		SADIActivityContextualViewFactory factory = null;
		for (ContextualViewFactory cvf : viewFactoriesForBeanType) {
			if (cvf instanceof SADIActivityContextualViewFactory) {
				factory = (SADIActivityContextualViewFactory) cvf;
			}
		}
		assertTrue("No SADI view factory", factory != null);
	}
	
	@Test
	@Ignore
	public void testConfigurationAction() throws Exception {
		SADIActivityContextualView view = new SADIActivityContextualView(activity);
		assertNotNull("The view should provide a configuration action",view.getConfigureAction(null));
		assertTrue("The configuration action should be an instance of SADIActivityConfigurationAction",view.getConfigureAction(null) instanceof SADIActivityConfigurationAction);
	}
	
}