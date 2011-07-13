package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.serviceprovider;

import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

import javax.swing.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidMinerServiceDesc extends ServiceDescription<RapidMinerActivityConfigurationBean> {

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<RapidMinerActivityConfigurationBean>> getActivityClass() {
		return RapidMinerExampleActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public RapidMinerActivityConfigurationBean getActivityConfiguration() {
		RapidMinerActivityConfigurationBean bean = new RapidMinerActivityConfigurationBean();
		bean.setOperatorName(operatorName);
		bean.setCallName(callName);

//		bean.setIsExplicit(true); // by default
//		bean.setUsername("");
//		bean.setPassword("");
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return RapidMinerIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return operatorName;
	}
	
	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	boolean set = false;
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		//List<String> myList = new ArrayList<String>();
		//myList.add("a path");
		//myList.add("another path");
		//myList.add("yet another path");
		
		//return Arrays.asList("e-LICO Rapid Miner Services @ " + exampleUri, "something");
		if (!set) {
			myPath.add(0, "Rapid Miner Services");
			set = true;
			return myPath;
		} else {
			return null;
		}
		
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(operatorName, exampleUri);
	}

	
	// FIXME: Replace example fields and getters/setters with any required
	// and optional fields. (All fields are searchable in the Service palette,
	// for instance try a search for exampleString:3)
	private String operatorName;
	private String callName;
	private URI exampleUri;
	private List<String> myPath = new ArrayList<String>();
	
	public String getOperatorName() {
		
		return operatorName;
	}
	
	public URI getExampleUri() {
		return exampleUri;
	}
	
	public void setOperatorName(String exampleString) {
		this.operatorName = exampleString;
	}
	
	public void setExampleUri(URI exampleUri) {
		this.exampleUri = exampleUri;
	}
	
	public void setMySetting(String a) {
		
	}
	
	public String getMySetting() {	
		return null;
	}
	
	public void setPath(List <String> myPathList) {
		//myPath.add("e-LICO");
		myPath = myPathList;
	
	}
	
	public void setCallName(String name) {
		callName = name;
	}
	
	public String getCallName() {
		return callName;
	}
	
	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return true;
	}
	


}
