/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.rosuda.REngine.Rserve.RserveException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A health checker for the Rshell activity.
 */
public class RshellActivityHealthChecker implements HealthChecker<RshellActivity> {

	public boolean canVisit(Object subject) {
		return (subject != null) && (subject instanceof RshellActivity);
	}

	public VisitReport visit(RshellActivity activity, List<Object> ancestors) {
		VisitReport vr = null;

		JsonNode config = activity.getConfiguration();
		if (config != null) {
			RshellConnectionSettings settings = activity.getConnectionSettings();
			RshellConnection connection = null;
			try {
				connection = RshellConnectionManager.INSTANCE.createConnection(settings);
			} catch (RserveException e) {
				vr = new VisitReport(HealthCheck.getInstance(), activity, "Read problem",
						HealthCheck.IO_PROBLEM, Status.SEVERE);
				vr.setProperty("exception", e);
				vr.setProperty("endpoint",
						"http://" + settings.getHost() + ":" + settings.getPort());
				return vr;
			} finally {
				if (connection != null && connection.isConnected()) {
					RshellConnectionManager.INSTANCE.releaseConnection(connection);
				}
			}
		}
		return null;
	}

	public boolean isTimeConsuming() {
		return true;
	}

}
