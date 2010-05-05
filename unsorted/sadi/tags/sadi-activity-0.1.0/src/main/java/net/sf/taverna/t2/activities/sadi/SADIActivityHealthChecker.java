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

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import ca.wilkinsonlab.sadi.client.Service.ServiceStatus;

/**
 * A health checker for a {@link SADIActivity}.
 * 
 * @author David Withers
 */
public class SADIActivityHealthChecker implements HealthChecker<SADIActivity> {

	private static final String OK_MESSAGE = "The service is OK";
	private static final String WARNING_MESSAGE = "The service is running slowly";
	private static final String SEVERE_MESSAGE = "The service is dead";
	private static final String ACTIVITY_NAME = "SADI Activity";

	public boolean canHandle(Object subject) {
		return (subject instanceof SADIActivity);
	}

	public HealthReport checkHealth(SADIActivity activity) {
		HealthReport healthReport = null;
		try {
			ServiceStatus serviceStatus = activity.getRegistry().getServiceStatus(
					activity.getConfiguration().getServiceURI());
			if (serviceStatus.equals(ServiceStatus.OK)) {
				healthReport = new HealthReport(ACTIVITY_NAME, OK_MESSAGE, Status.OK);
			} else if (serviceStatus.equals(ServiceStatus.SLOW)) {
				healthReport = new HealthReport(ACTIVITY_NAME, WARNING_MESSAGE,
						Status.WARNING);
			} else if (serviceStatus.equals(ServiceStatus.DEAD)) {
				healthReport = new HealthReport(ACTIVITY_NAME, SEVERE_MESSAGE,
						Status.SEVERE);
			}
		} catch (IOException e) {
			healthReport = new HealthReport(ACTIVITY_NAME, "Unable to contact service",
					Status.SEVERE);
		}
		return healthReport;
	}

}
