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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.usecase.KnowARCConfigurationFactory;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;

/**
 * UseCaseServiceProvider searches an use case repository XML for use case
 * descriptions.
 * 
 * @author Hajo Nils Krabbenhšft
 */
public class UseCaseServiceProvider extends AbstractConfigurableServiceProvider<UseCaseServiceProviderConfig> {

	public UseCaseServiceProvider() {
		super(new UseCaseServiceProviderConfig("http://somehost/service?usecase"));
	}

	public String getName() {
		return "UseCase Service";
	}

	public List<UseCaseServiceProviderConfig> getDefaultConfigurations() {
		List<UseCaseServiceProviderConfig> defaults = new ArrayList<UseCaseServiceProviderConfig>();
		// the default use case repository is our shared repository on
		// taverna.nordugrid.org
		defaults.add(new UseCaseServiceProviderConfig("http://taverna.nordugrid.org/sharedRepository/xml.php"));
		return defaults;
	}

	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		String repositoryUrl = serviceProviderConfig.getRepositoryUrl();
		callBack.status("Parsing use case repository:" + repositoryUrl);
		try {
			// prepare a list of all use case descriptions which are stored in
			// the given repository URL
			List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
					repositoryUrl);
			callBack.status("Found " + usecases.size() + " use cases:" + repositoryUrl);
			// convert all the UseCaseDescriptions in the XML file into
			// displayeable UseCaseServiceDescription items
			List<UseCaseServiceDescription> items = new ArrayList<UseCaseServiceDescription>();
			for (UseCaseDescription usecase : usecases) {
				UseCaseServiceDescription item = new UseCaseServiceDescription();
				item.setRepositoryUrl(repositoryUrl);
				item.setUsecaseid(usecase.usecaseid);
				items.add(item);
			}
			// we dont have streaming data loading or partial results, so return
			// results and finish
			callBack.partialResults(items);
			callBack.finished();
		} catch (Exception e) {
			callBack.fail("error", e);
		}
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration().getRepositoryUrl();
	}

	public Icon getIcon() {
		return KnowARCConfigurationFactory.getConfiguration().getIcon();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		List<String> result;
		// one can fully identify an use case repository by its URL
		result = Arrays.asList(getConfiguration().getRepositoryUrl());
		return result;
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry registry) {
	}

}
