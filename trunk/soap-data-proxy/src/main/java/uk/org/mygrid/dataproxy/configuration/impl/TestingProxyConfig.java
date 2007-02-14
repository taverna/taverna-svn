/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: TestingProxyConfig.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-14 14:07:17 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDef;


public class TestingProxyConfig implements ProxyConfig {
	
	Map<String,WSDLConfig> wsdlConfigs = new HashMap<String,WSDLConfig>();
	
	public TestingProxyConfig() {
		List<ElementDef> elements = new ArrayList<ElementDef>();
		List<String>replacements = new ArrayList<String>();
		
		elements.add(new ElementDef("FieldList","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo"));
		replacements.add("FieldList-replaced");
		elements.add(new ElementDef("Link","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo"));
		replacements.add("Link-replaced");
		wsdlConfigs.put("11111",new TestingWSDLConfig("11111",elements,replacements,"http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/soap_adapter_1_5.cgi"));
	}

	public URL getStoreBaseURL() {
		try {
			return new URL("file:/tmp/proxy-testing/");
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		}
		return null;
	}

	public WSDLConfig getWSDLConfigForID(String ID) {
		return wsdlConfigs.get(ID);
	}
	
	class TestingWSDLConfig implements WSDLConfig {

		private String id;
		private List<ElementDef> elements = new ArrayList<ElementDef>();
		private Map<ElementDef,String> replacements = new HashMap<ElementDef, String>();
		private String endpoint;
		
		
		public TestingWSDLConfig(String id, List<ElementDef> elements, List<String> replacements, String endpoint) {
			this.id=id;
			this.endpoint=endpoint;
			int i=0;
			for (ElementDef def : elements) {				
				this.elements.add(def);
				this.replacements.put(def,replacements.get(i++));
			}
		}
		
		public String getEndpoint() {
			return this.endpoint;
		}

		public List<ElementDef> getElements() {
			return this.elements;
		}

		public String getReplacement(ElementDef element) {
			return this.replacements.get(element);
		}

		public String getWSDLID() {
			return id;
		}
		
	}
		

}
