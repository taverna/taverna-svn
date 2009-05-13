/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.webservice.resource;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A collection of jobs.
 *
 * @author David Withers
 */
@XmlRootElement(name = "Jobs")
public class Jobs {

	private Collection<JobResource> jobs;
	
	public Jobs() {
		this.jobs = new ArrayList<JobResource>();
	}

	public Jobs(Collection<JobResource> jobs) {
		this.jobs = jobs;
	}

	public Collection<JobResource> getJob() {
		return jobs;
	}

	public void setJob(Collection<JobResource> jobs) {
		this.jobs = jobs;
	}
	
}
