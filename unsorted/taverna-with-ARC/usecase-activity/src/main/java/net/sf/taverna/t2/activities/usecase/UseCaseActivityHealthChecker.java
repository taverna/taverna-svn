/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhšft, INB, University of Luebeck   
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

package net.sf.taverna.t2.activities.usecase;

import java.util.ArrayList;
import java.util.List;

import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * Investigates if everything is going fine with a job
 * 
 * @author Hajo Nils Krabbenhšft
 */
public class UseCaseActivityHealthChecker implements HealthChecker<UseCaseActivity> {

	public boolean canHandle(Object subject) {
		return subject != null && subject instanceof UseCaseActivity;
	}

	public HealthReport checkHealth(UseCaseActivity activity) {
		UseCaseActivityConfigurationBean configuration = activity.getConfiguration();
		List<HealthReport> reports = new ArrayList<HealthReport>();

		// currently a use case is doing fine if the repository is fine and
		// contains the needed use case
		reports.add(checkRepository(configuration));
		reports.add(checkUsecase(configuration));

		Status status = highestStatus(reports);
		HealthReport report = new HealthReport("Janitor Use Case Activity", "", status, reports);

		return report;
	}

	private HealthReport checkRepository(UseCaseActivityConfigurationBean configuration) {
		try {
			// try to parse the use case repository XML file
			UseCaseEnumeration.enumerateXmlInner(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()), configuration.getRepositoryUrl(),
					new ArrayList<UseCaseDescription>());
		} catch (Throwable e) {
			return new HealthReport("checkRepository", "Could not enumerate repository \"" + configuration.getRepositoryUrl() + "\" due to error: "
					+ e.toString(), Status.SEVERE);
		}
		return new HealthReport("checkRepository", "Repository is fine: " + configuration.getRepositoryUrl(), Status.OK);
	}

	private HealthReport checkUsecase(UseCaseActivityConfigurationBean configuration) {
		// get a list of use cases from the repository XML file
		List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
				configuration.getRepositoryUrl());
		// search for the needed use case
		for (UseCaseDescription usecase : usecases) {
			if (!usecase.usecaseid.equalsIgnoreCase(configuration.getUsecaseid()))
				continue;
			return new HealthReport("checkUsecase", "Usecase " + configuration.getUsecaseid() + " was found.", Status.OK);
		}

		return new HealthReport("checkUsecase", "Could not find usecase: " + configuration.getUsecaseid(), Status.SEVERE);
	}

	private Status highestStatus(List<HealthReport> reports) {
		Status status = Status.OK;
		for (HealthReport report : reports) {
			if (report.getStatus().equals(Status.WARNING) && status.equals(Status.OK))
				status = report.getStatus();
			if (report.getStatus().equals(Status.SEVERE))
				status = Status.SEVERE;
		}
		return status;
	}

}
