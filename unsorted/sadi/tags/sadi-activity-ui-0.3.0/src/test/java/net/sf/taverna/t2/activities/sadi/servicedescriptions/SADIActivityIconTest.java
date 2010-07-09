/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.servicedescriptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SADIActivityIcon}.
 *
 * @author David Withers
 */
public class SADIActivityIconTest {

	private SADIActivityIcon sadiActivityIcon;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiActivityIcon = new SADIActivityIcon();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIActivityIcon#canProvideIconScore(net.sf.taverna.t2.workflowmodel.processor.activity.Activity)}.
	 */
	@Test
	public void testCanProvideIconScore() {
		assertEquals(ActivityIconSPI.NO_ICON, sadiActivityIcon.canProvideIconScore(null));
		assertEquals(ActivityIconSPI.DEFAULT_ICON + 1, sadiActivityIcon.canProvideIconScore(new SADIActivity()));
		assertEquals(ActivityIconSPI.NO_ICON, sadiActivityIcon.canProvideIconScore(new AbstractActivity<String>() {
			public void configure(String conf) throws ActivityConfigurationException {
			}
			public String getConfiguration() {
				return null;
			}
		}));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIActivityIcon#getIcon(net.sf.taverna.t2.workflowmodel.processor.activity.Activity)}.
	 */
	@Test
	public void testGetIcon() {
		assertEquals(SADIActivityIcon.getSADIIcon(), sadiActivityIcon.getIcon(new SADIActivity()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIActivityIcon#getSADIIcon()}.
	 */
	@Test
	public void testGetSADIIcon() {
		assertNotNull(SADIActivityIcon.getSADIIcon());
	}

}
