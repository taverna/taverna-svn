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
package net.sf.taverna.t2.activities.rshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityHealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for RshellActivityHealthChecker.
 * 
 */
public class RshellActivityHealthCheckerTest {

	private RshellActivity activity;
	
	private RshellActivityHealthChecker activityHealthChecker;
	
	@Before
	public void setUp() throws Exception {
		activity = new RshellActivity();
		activityHealthChecker = new RshellActivityHealthChecker();
	}

	@Test
	public void testCanHandle() {
		assertFalse(activityHealthChecker.canVisit(null));
		assertFalse(activityHealthChecker.canVisit(new Object()));
		assertFalse(activityHealthChecker.canVisit(new AbstractActivity<Object>() {
			public void configure(Object conf) throws ActivityConfigurationException {
			}
			public Object getConfiguration() {
				return null;
			}
		}));
		assertTrue(activityHealthChecker.canVisit(activity));
	}

	@Test
	public void testCheckHealth() {
		VisitReport healthReport = activityHealthChecker.visit(activity, new ArrayList());
		if (healthReport != null) {
		    assertEquals(Status.SEVERE, healthReport.getStatus());
		}
	}

}
