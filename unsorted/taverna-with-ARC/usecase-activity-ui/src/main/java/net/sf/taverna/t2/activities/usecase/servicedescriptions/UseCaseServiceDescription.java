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

package net.sf.taverna.t2.activities.usecase.servicedescriptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.usecase.KnowARCConfigurationFactory;
import net.sf.taverna.t2.activities.usecase.UseCaseActivity;
import net.sf.taverna.t2.activities.usecase.UseCaseActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * UseCaseServiceDescription stores the repository URL and the use case id so
 * that it can create an UseCaseActivityConfigurationBean
 * 
 * @author Hajo Nils Krabbenhšft
 */
public class UseCaseServiceDescription extends ServiceDescription<UseCaseActivityConfigurationBean> {

	private String repositoryUrl;
	private String usecaseid;

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public String getUsecaseid() {
		return usecaseid;
	}

	public void setUsecaseid(String usecaseid) {
		this.usecaseid = usecaseid;
	}

	public Icon getIcon() {
		return KnowARCConfigurationFactory.getConfiguration().getIcon();
	}

	public Class<? extends Activity<UseCaseActivityConfigurationBean>> getActivityClass() {
		return UseCaseActivity.class;
	}

	public UseCaseActivityConfigurationBean getActivityConfiguration() {
		UseCaseActivityConfigurationBean bean = new UseCaseActivityConfigurationBean();
		bean.setRepositoryUrl(repositoryUrl);
		bean.setUsecaseid(usecaseid);
		return bean;
	}

	public String getName() {
		return usecaseid;
	}

	@SuppressWarnings("unchecked")
	public List<? extends Comparable> getPath() {
		return Collections.singletonList("UseCase @ " + repositoryUrl);
	}

	protected List<Object> getIdentifyingData() {
		// we require use cases inside one XML file to have unique IDs, which
		// means every usecase is uniquely identified by its repository URL and
		// its use case ID.
		return Arrays.<Object> asList(repositoryUrl, usecaseid);
	}

}
