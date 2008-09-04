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
package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class ActivityPaletteConfiguration extends AbstractConfigurable {
	
	private Map<String,String> defaultPropertyMap;
	
	private static ActivityPaletteConfiguration instance = new ActivityPaletteConfiguration();

	private ActivityPaletteConfiguration() {

	}

	public static ActivityPaletteConfiguration getInstance() {
		return instance;
	}

	public String getCategory() {
		return "activities";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap==null) {
			defaultPropertyMap = new HashMap<String, String>();
		
			//wsdl
			defaultPropertyMap.put("taverna.defaultwsdl", "http://www.mygrid.org.uk/taverna-tests/testwsdls/KEGG.wsdl,http://www.mygrid.org.uk/taverna-tests/testwsdls/whatizit.wsdl");
			
			//soaplab
			defaultPropertyMap.put("taverna.defaultsoaplab", "http://www.ebi.ac.uk/soaplab/services/");
			
			//biomart
			defaultPropertyMap.put("taverna.defaultmartregistry","http://www.biomart.org/biomart");
			
			//add property names
			defaultPropertyMap.put("name.taverna.defaultwsdl", "WSDL");
			defaultPropertyMap.put("name.taverna.defaultsoaplab","Soaplab");
			defaultPropertyMap.put("name.taverna.defaultmartregistry", "Biomart");
		}
		return defaultPropertyMap;
	}

	public String getName() {
		return "Activity Palette";
	}

	public String getUUID() {
		return "ad9f3a60-5967-11dd-ae16-0800200c9a66";
	}

}
