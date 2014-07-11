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
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.client.ServiceStatus;

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
			public Registry getRegistry() throws IOException {
				return new RegistryImpl(new BaseConfiguration() {}) {
					@Override
					public ServiceStatus getServiceStatus(String serviceURI) throws SADIException {
						return serviceStatus;
					}
				};
			}

			@Override
			public SADIActivityConfigurationBean getConfiguration() {
				return new SADIActivityConfigurationBean() {
					public String getServiceURI() {
						return "http://example.com/test";
					}					
				};
			}
		};
		activityHealthChecker = new SADIActivityHealthChecker();
	}

	@Test
	public void testCanVisit() {
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

	@Ignore @Test
	// TODO Fix this
	public void testVisit() {
		serviceStatus = ServiceStatus.OK;
		VisitReport healthReport = activityHealthChecker.visit(activity, new ArrayList<Object>());
		assertNotNull(healthReport);
		assertEquals(HealthCheck.NO_PROBLEM, healthReport.getResultId());
		assertEquals(Status.OK, healthReport.getStatus());		
		assertEquals(2, healthReport.getSubReports().size());
		Iterator<VisitReport> iterator = healthReport.getSubReports().iterator();
		VisitReport subReport = iterator.next();
		assertEquals(Status.OK, subReport.getStatus());
		subReport = iterator.next();
		assertEquals(Status.OK, subReport.getStatus());

		serviceStatus = ServiceStatus.SLOW;
		healthReport = activityHealthChecker.visit(activity, new ArrayList<Object>());
		assertNotNull(healthReport);
		assertEquals(HealthCheck.NO_PROBLEM, healthReport.getResultId());
		assertEquals(Status.WARNING, healthReport.getStatus());		
		assertEquals(2, healthReport.getSubReports().size());
		iterator = healthReport.getSubReports().iterator();
		subReport = iterator.next();
		assertEquals(Status.OK, subReport.getStatus());
		subReport = iterator.next();
		assertEquals(Status.WARNING, subReport.getStatus());

		serviceStatus = ServiceStatus.DEAD;
		healthReport = activityHealthChecker.visit(activity, new ArrayList<Object>());
		assertNotNull(healthReport);
		assertEquals(HealthCheck.NO_PROBLEM, healthReport.getResultId());
		assertEquals(Status.SEVERE, healthReport.getStatus());		
		assertEquals(2, healthReport.getSubReports().size());
		iterator = healthReport.getSubReports().iterator();
		subReport = iterator.next();
		assertEquals(Status.OK, subReport.getStatus());
		subReport = iterator.next();
		assertEquals(Status.SEVERE, subReport.getStatus());

		healthReport = activityHealthChecker.visit(new SADIActivity() {
			public RegistryImpl getRegistry() throws IOException {
				throw new IOException();
			}
			public SADIActivityConfigurationBean getConfiguration() {
				return new SADIActivityConfigurationBean() {
					public String getServiceURI() {
						return "http://example.com/test";
					}					
				};
			}
		}, new ArrayList<Object>());
		assertNotNull(healthReport);
		assertEquals(HealthCheck.NO_PROBLEM, healthReport.getResultId());
		assertEquals(Status.WARNING, healthReport.getStatus());		
		assertEquals(2, healthReport.getSubReports().size());
		healthReport.getSubReports().iterator().next();
		iterator = healthReport.getSubReports().iterator();
		iterator.next();
		subReport = iterator.next();
		assertEquals(Status.WARNING, subReport.getStatus());

		healthReport = activityHealthChecker.visit(new SADIActivity() {
			public RegistryImpl getRegistry() throws IOException {
				return new RegistryImpl(new BaseConfiguration() {}) {
					public ServiceStatus getServiceStatus(String serviceURI) throws SADIException {
						throw new SADIException("");
					}
				};
			}
			public SADIActivityConfigurationBean getConfiguration() {
				return new SADIActivityConfigurationBean() {
					public String getServiceURI() {
						return "http://example.com/test";
					}					
				};
			}
		}, new ArrayList<Object>());
		assertNotNull(healthReport);
		assertEquals(HealthCheck.NO_PROBLEM, healthReport.getResultId());
		assertEquals(Status.WARNING, healthReport.getStatus());		
		assertEquals(2, healthReport.getSubReports().size());
		healthReport.getSubReports().iterator().next();
		iterator = healthReport.getSubReports().iterator();
		iterator.next();
		subReport = iterator.next();
		assertEquals(Status.WARNING, subReport.getStatus());
	}

	@Test
	public void testIsTimeConsuming() {
		assertTrue(activityHealthChecker.isTimeConsuming());
	}

}
