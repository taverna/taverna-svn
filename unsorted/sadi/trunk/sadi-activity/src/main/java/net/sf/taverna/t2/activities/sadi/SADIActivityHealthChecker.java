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

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.client.Service.ServiceStatus;
import ca.wilkinsonlab.sadi.common.SADIException;

/**
 * A {@link HealthChecker} for a {@link SADIActivity}.
 * 
 * @author David Withers
 */
public class SADIActivityHealthChecker implements HealthChecker<SADIActivity> {

	private static Logger logger = Logger.getLogger(SADIActivityHealthChecker.class);

	private static final String OK_MESSAGE = "The service is OK";
	private static final String WARNING_MESSAGE = "The service is running slowly";
	private static final String SEVERE_MESSAGE = "The service is dead";

	public static final int SADI_SERVICE_SLOW = 101;
	public static final int SADI_SERVICE_DEAD = 102;

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
//		reports.add(checkSADIRegistry(activity, registryURI, serviceURI));
		
		Status status = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "SADI Activity Report", HealthCheck.NO_PROBLEM,
				status, reports);

		return report;
	}
	
	/**
	 * @param serviceURI
	 * @return
	 */
	private VisitReport checkSADIRegistry(SADIActivity activity, String registryURI, String serviceURI) {
		VisitReport visitReport = null;
		try {
			// Registry.getServiceStatus is throwing an OperationNotSupportedException
			ServiceStatus serviceStatus = activity.getRegistry().getServiceStatus(serviceURI);
			if (serviceStatus.equals(ServiceStatus.OK)) {
				visitReport = new VisitReport(HealthCheck.getInstance(), activity, OK_MESSAGE, HealthCheck.NO_PROBLEM, Status.OK);
			}
			else if (serviceStatus.equals(ServiceStatus.SLOW)) {
				visitReport = new VisitReport(HealthCheck.getInstance(), activity, WARNING_MESSAGE, SADI_SERVICE_SLOW, Status.WARNING);
				visitReport.setProperty(SADI_REGISTRY_URI_PROPERTY, registryURI);
				visitReport.setProperty(SADI_SERVICE_URI_PROPERTY, serviceURI);
			} else {
				visitReport = new VisitReport(HealthCheck.getInstance(), activity, SEVERE_MESSAGE, SADI_SERVICE_DEAD, Status.SEVERE);
				visitReport.setProperty(SADI_REGISTRY_URI_PROPERTY, registryURI);
				visitReport.setProperty(SADI_SERVICE_URI_PROPERTY, serviceURI);
			}
		} catch (IOException e) {
			visitReport = new VisitReport(HealthCheck.getInstance(), activity, e.getMessage(), HealthCheck.CONNECTION_PROBLEM, Status.WARNING);
			visitReport.setProperty("exception", e);
		} catch (SADIException e) {
			visitReport = new VisitReport(HealthCheck.getInstance(), activity, e.getMessage(), HealthCheck.CONNECTION_PROBLEM, Status.WARNING);
			visitReport.setProperty("exception", e);
		}
		return visitReport;
	}

	public boolean isTimeConsuming() {
		return true;
	}

}
