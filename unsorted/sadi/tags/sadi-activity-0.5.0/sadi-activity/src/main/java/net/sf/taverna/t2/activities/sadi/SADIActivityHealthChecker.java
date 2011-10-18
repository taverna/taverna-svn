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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;
import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.client.ServiceStatus;

/**
 * A {@link HealthChecker} for a {@link SADIActivity}.
 * 
 * @author David Withers
 */
public class SADIActivityHealthChecker implements HealthChecker<SADIActivity> {

//	private static Logger logger = Logger.getLogger(SADIActivityHealthChecker.class);

	private static final String OK_MESSAGE = "The service is OK";
	private static final String SLOW_MESSAGE = "The service is running slowly";
	private static final String INCORRECT_MESSAGE = "The service is failing test cases";
	private static final String DEAD_MESSAGE = "The service is not responding";

	public static final int SADI_SERVICE_SLOW = 101;
	public static final int SADI_SERVICE_DEAD = 102;
	public static final int SADI_SERVICE_INCORRECT = 103;

	public static final String SADI_REGISTRY_URI_PROPERTY = "sadiRegistryUri";
	public static final String SADI_SERVICE_URI_PROPERTY = "sadiServiceUri";
	
	public boolean canVisit(Object subject) {
		return (subject instanceof SADIActivity);
	}

	public VisitReport visit(SADIActivity activity, List<Object> ancestors) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
		String registryURI = activity.getConfiguration().getSparqlEndpoint();
		String serviceURI = activity.getConfiguration().getServiceURI();
		reports.add(RemoteHealthChecker.contactEndpoint(activity, serviceURI));
		reports.add(checkSADIRegistry(activity, registryURI, serviceURI));
	
		Status status = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "SADI Activity Report", HealthCheck.NO_PROBLEM,
				status, reports);

		return report;
	}
	
//	/**
//	 * Invoke the service on it's described test cases and check the actual
//	 * output against the expected output.
//	 * This can be extremely slow and it's probably better to just read the
//	 * most recent test results from the registry.
//	 * @param activity the SADI activity
//	 * @param serviceURI the service URI
//	 * @return a VisitReport
//	 */
//	protected VisitReport testSADIService(SADIActivity activity, String serviceURI) {
//		VisitReport visitReport = null;
//		try {
//			ServiceTester.testService(new ServiceImpl(serviceURI), false);
//			visitReport = new VisitReport(HealthCheck.getInstance(), activity, OK_MESSAGE, HealthCheck.NO_PROBLEM, Status.OK);
//		} catch (ServiceInvocationException e) {
//			visitReport = new VisitReport(HealthCheck.getInstance(), activity, DEAD_MESSAGE, SADI_SERVICE_DEAD, Status.SEVERE);
//		} catch (SADIException e) {
//			visitReport = new VisitReport(HealthCheck.getInstance(), activity, INCORRECT_MESSAGE, SADI_SERVICE_INCORRECT, Status.WARNING);
//		}
//		return visitReport;
//	}
	
	private VisitReport checkSADIRegistry(SADIActivity activity, String registryURI, String serviceURI) {
		VisitReport visitReport = null;
		try {
			ServiceStatus serviceStatus = activity.getRegistry().getServiceStatus(serviceURI);
			switch (serviceStatus) {
				case OK: 
					visitReport = new VisitReport(HealthCheck.getInstance(), activity, OK_MESSAGE, HealthCheck.NO_PROBLEM, Status.OK);
					break;
				case SLOW:
					visitReport = new VisitReport(HealthCheck.getInstance(), activity, SLOW_MESSAGE, SADI_SERVICE_SLOW, Status.WARNING);
					visitReport.setProperty(SADI_REGISTRY_URI_PROPERTY, registryURI);
					visitReport.setProperty(SADI_SERVICE_URI_PROPERTY, serviceURI);
					break;
				case DEAD:
					visitReport = new VisitReport(HealthCheck.getInstance(), activity, DEAD_MESSAGE, SADI_SERVICE_DEAD, Status.SEVERE);
					visitReport.setProperty(SADI_REGISTRY_URI_PROPERTY, registryURI);
					visitReport.setProperty(SADI_SERVICE_URI_PROPERTY, serviceURI);
					break;
				case INCORRECT:
					visitReport = new VisitReport(HealthCheck.getInstance(), activity, INCORRECT_MESSAGE, SADI_SERVICE_INCORRECT, Status.WARNING);
					visitReport.setProperty(SADI_REGISTRY_URI_PROPERTY, registryURI);
					visitReport.setProperty(SADI_SERVICE_URI_PROPERTY, serviceURI);
					break;
				default:
					break;
			}
		} catch (IOException e) {
			// invalid SPARQL endpoint URL (i.e.: new URL(...) throws IOException)
			visitReport = new VisitReport(HealthCheck.getInstance(), activity, String.format("invalid registry URL %s", registryURI), HealthCheck.INVALID_URL, Status.WARNING);
			visitReport.setProperty("exception", e);
		} catch (SADIException e) {
			// problem contacting the registry...
			visitReport = new VisitReport(HealthCheck.getInstance(), activity, e.getMessage(), HealthCheck.CONNECTION_PROBLEM, Status.WARNING);
			visitReport.setProperty("exception", e);
		}
		return visitReport;
	}

	public boolean isTimeConsuming() {
		return true;
	}
}
