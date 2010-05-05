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

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.client.Service.ServiceStatus;
import ca.wilkinsonlab.sadi.common.SADIException;

/**
 * Unit tests for {@link SADIActivityHealthChecker}.
 * 
 * @author David Withers
 */
public class SADIActivityHealthCheckerTest {

	private SADIActivity activity;
	
	private SADIActivityHealthChecker activityHealthChecker;
	
	private ServiceStatus serviceStatus;
		
	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity() {
			@Override
			public RegistryImpl getRegistry() throws IOException {
				return new RegistryImpl(new BaseConfiguration() {}) {
					@Override
					public ServiceStatus getServiceStatus(String serviceURI) throws SADIException {
						return serviceStatus;
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
		serviceStatus = ServiceStatus.OK;
		HealthReport healthReport = activityHealthChecker.checkHealth(activity);
		assertNotNull(healthReport);
		assertEquals(Status.OK, healthReport.getStatus());
		serviceStatus = ServiceStatus.SLOW;
		healthReport = activityHealthChecker.checkHealth(activity);
		assertNotNull(healthReport);
		assertEquals(Status.WARNING, healthReport.getStatus());
		serviceStatus = ServiceStatus.DEAD;
		healthReport = activityHealthChecker.checkHealth(activity);
		assertNotNull(healthReport);
		assertEquals(Status.SEVERE, healthReport.getStatus());
		healthReport = activityHealthChecker.checkHealth(new SADIActivity() {
			public RegistryImpl getRegistry() throws IOException {
				throw new IOException();
			}
		});
		assertNotNull(healthReport);
		assertEquals(Status.SEVERE, healthReport.getStatus());
		healthReport = activityHealthChecker.checkHealth(new SADIActivity() {
			public RegistryImpl getRegistry() throws IOException {
				return new RegistryImpl(new BaseConfiguration() {}) {
					public ServiceStatus getServiceStatus(String serviceURI) throws SADIException {
						throw new SADIException("");
					}
				};
			}
			public SADIActivityConfigurationBean getConfiguration() {
				return new SADIActivityConfigurationBean();
			}
		});
		assertNotNull(healthReport);
		assertEquals(Status.SEVERE, healthReport.getStatus());
	}

}
