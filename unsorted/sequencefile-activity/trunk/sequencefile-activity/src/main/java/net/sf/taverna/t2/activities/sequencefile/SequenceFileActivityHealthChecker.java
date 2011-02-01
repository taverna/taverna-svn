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
package net.sf.taverna.t2.activities.sequencefile;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * A health checker for the SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileActivityHealthChecker implements HealthChecker<SequenceFileActivity> {

	public boolean canVisit(Object subject) {
		return (subject instanceof SequenceFileActivity);
	}

	public boolean isTimeConsuming() {
		return false;
	}

	public VisitReport visit(SequenceFileActivity activity, List<Object> ancestors) {
		Object subject = (Processor) VisitReport.findAncestor(ancestors, Processor.class);
		if (subject == null) {
			// Fall back to using the activity itself as the subject of the reports
			subject = activity;
		}
		return new VisitReport(HealthCheck.getInstance(), subject, "Sequence File Reader Report", HealthCheck.NO_PROBLEM, Status.OK);
	}

}
