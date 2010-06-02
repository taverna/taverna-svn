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
package net.sf.taverna.t2.activities.apiconsumer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity.FileExtFilter;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * Health checker for the ApiConsumerActivity.
 * 
 * @author Alex Nenadic
 */
public class ApiConsumerActivityHealthChecker implements HealthChecker<ApiConsumerActivity>{

	public boolean canHandle(Object subject) {
		return (subject!=null && subject instanceof ApiConsumerActivity);
	}

	public HealthReport checkHealth(ApiConsumerActivity subject) {
		// Check if we can find the jar containing the apiconsumer's class
		try {
			// Try to load the API consumer's class
			subject.getClassLoader().loadClass(subject.getConfiguration().getClassName());
			// All is fine
		} catch (ClassNotFoundException e) {
			return new HealthReport("ApiConsumerActivity", "API consumer's class missing", Status.SEVERE);
		}

		// Check if we can find all the API consumer's dependencies
		LinkedHashSet<String> localDependencies = subject.getConfiguration().getLocalDependencies();
		List<String> jarFiles = Arrays.asList(ApiConsumerActivity.libDir.list(new FileExtFilter(".jar"))); // URLs of all jars found in the lib directory 
		for (String jar : localDependencies) {
			if (jarFiles.contains(jar)){
				localDependencies.remove(jar);
			}
		}
		if (localDependencies.isEmpty()){ // all dependencies found
			return new HealthReport("ApiConsumerActivity", "API consumer's class and all its dependencies found", Status.OK);
		}
		else{
			return new HealthReport("ApiConsumerActivity", "Some API consumer's dependencies missing", Status.SEVERE);
		}
	}

}
