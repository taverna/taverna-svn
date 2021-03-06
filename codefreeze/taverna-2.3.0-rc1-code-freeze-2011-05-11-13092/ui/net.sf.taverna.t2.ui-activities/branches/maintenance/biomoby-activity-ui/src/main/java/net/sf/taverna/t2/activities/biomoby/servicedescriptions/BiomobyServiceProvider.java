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
package net.sf.taverna.t2.activities.biomoby.servicedescriptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.biomoby.query.BiomobyActivityIcon;
import net.sf.taverna.t2.activities.biomoby.query.BiomobyQueryHelper;
import net.sf.taverna.t2.activities.biomoby.ui.AddBiomobyDialogue;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.MobyException;

public class BiomobyServiceProvider extends
		AbstractConfigurableServiceProvider<BiomobyServiceProviderConfig>
		implements
		CustomizedConfigurePanelProvider<BiomobyServiceProviderConfig> {

	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/biomoby");
	
	public BiomobyServiceProvider() {
		super(new BiomobyServiceProviderConfig());
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		try {
			// constructor throws exception if it cannot communicate
			// with the registry
			BiomobyQueryHelper helper = new BiomobyQueryHelper(
					getConfiguration().getEndpoint().toASCIIString(),
					getConfiguration().getNamespace().toASCIIString());
			helper.findServiceDescriptionsAsync(callBack);
		} catch (MobyException ex) {
			callBack.fail("Could not connect to Biomoby endpoint "
					+ getConfiguration().getEndpoint(), ex);
		}
	}

	@Override
	public List<BiomobyServiceProviderConfig> getDefaultConfigurations() {
		
		List<BiomobyServiceProviderConfig> defaults = new ArrayList<BiomobyServiceProviderConfig>();

		ServiceDescriptionRegistryImpl serviceRegistry = ServiceDescriptionRegistryImpl.getInstance();
		// If defaults have failed to load from a configuration file then load them here.
		if (!serviceRegistry.isDefaultSystemConfigurableProvidersLoaded()){
			defaults.add(new BiomobyServiceProviderConfig(
					CentralImpl.DEFAULT_ENDPOINT,
					CentralImpl.DEFAULT_NAMESPACE));
		} // else return an empty list
		
		return defaults;
	}

	public String getName() {
		return "Biomoby service";
	}

	public Icon getIcon() {
		return BiomobyActivityIcon.getBiomobyIcon();
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration().getEndpoint();
	}

	@SuppressWarnings("serial")
	public void createCustomizedConfigurePanel(
			final CustomizedConfigureCallBack<BiomobyServiceProviderConfig> callBack) {
		AddBiomobyDialogue addBiomobyDialogue = new AddBiomobyDialogue() {
			@Override
			protected void addRegistry(String registryEndpoint,
					String registryURI) {
				BiomobyServiceProviderConfig providerConfig = new BiomobyServiceProviderConfig(
						registryEndpoint, registryURI);
				callBack.newProviderConfiguration(providerConfig);
			}
		};
		addBiomobyDialogue.setVisible(true);
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		List<String> result;
		result = Arrays.asList(getConfiguration().getEndpoint().toString());
		return result;
	}

	public String getId() {
		return providerId.toString();
	}

}
