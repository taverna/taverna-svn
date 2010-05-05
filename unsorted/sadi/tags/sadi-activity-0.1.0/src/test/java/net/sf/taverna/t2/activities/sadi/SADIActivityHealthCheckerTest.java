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
package net.sf.taverna.t2.activities.sadi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.client.Service.ServiceStatus;
import ca.wilkinsonlab.sadi.rdf.RdfRegistry;

/**
 * Unit tests for SADIActivityHealthChecker.
 * 
 * @author David Withers
 */
public class SADIActivityHealthCheckerTest {

	private SADIActivity activity;
	
	private SADIActivityHealthChecker activityHealthChecker;
	
	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity() {
			@Override
			public RdfRegistry getRegistry() throws IOException {
				return new RdfRegistry("http://example.com/") {
					@Override
					public ServiceStatus getServiceStatus(String serviceURI) throws IOException {
						return ServiceStatus.OK;
					}
				};
			}

			@Override
			public SADIActivityConfigurationBean getConfiguration() {
				return new SADIActivityConfigurationBean();
			}
		};
		activityHealthChecker = new SADIActivityHealthChecker();
	}

	@Test
	public void testCanHandle() {
		assertFalse(activityHealthChecker.canHandle(null));
		assertFalse(activityHealthChecker.canHandle(new Object()));
		assertFalse(activityHealthChecker.canHandle(new AbstractActivity<Object>() {
			public void configure(Object conf) throws ActivityConfigurationException {
			}
			public Object getConfiguration() {
				return null;
			}
		}));
		assertTrue(activityHealthChecker.canHandle(activity));
	}

	@Test
	public void testCheckHealth() {
		HealthReport healthReport = activityHealthChecker.checkHealth(activity);
		assertNotNull(healthReport);
		assertEquals(Status.OK, healthReport.getStatus());
	}

}
