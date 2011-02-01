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
package net.sf.taverna.t2.activities.sequencefile.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivity;
import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean;
import net.sf.taverna.t2.activities.sequencefile.actions.SequenceFileActivityConfigurationAction;
import net.sf.taverna.t2.activities.sequencefile.views.SequenceFileActivityContextualView;
import net.sf.taverna.t2.activities.sequencefile.views.SequenceFileActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for SequenceFileActivityContextualView.
 *
 * @author David Withers
 */
public class TestSequenceFileActivityContextualView {

	SequenceFileActivity activity;

	@Before
	public void setup() throws Exception {
		activity = new SequenceFileActivity();		
		activity.configure(new SequenceFileActivityConfigurationBean());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		List<ContextualViewFactory> viewFactoriesForBeanType = ContextualViewFactoryRegistry.getInstance().getViewFactoriesForObject(activity);
		assertTrue("The view factory should not be empty", !viewFactoriesForBeanType.isEmpty());
		SequenceFileActivityViewFactory factory = null;
		for (ContextualViewFactory cvf : viewFactoriesForBeanType) {
			if (cvf instanceof SequenceFileActivityViewFactory) {
				factory = (SequenceFileActivityViewFactory) cvf;
			}
		}
		assertTrue("No Example view factory", factory != null);
	}
	
	@Test
	public void testConfigurationAction() throws Exception {
		SequenceFileActivityContextualView view = new SequenceFileActivityContextualView(activity);
		assertNotNull("The view should provide a configuration action",view.getConfigureAction(null));
		assertTrue("The configuration action should be an instance of SequenceFileActivityConfigurationAction",view.getConfigureAction(null) instanceof SequenceFileActivityConfigurationAction);
	}

}
