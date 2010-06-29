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

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.taverna.t2.service.model.Data;

@XmlRootElement(name = "Data")
public class DataResource extends Resource {
	
	private Map<String, DataValue> dataMap;
	
	public DataResource() {
	}

	public DataResource(Data data, URI uri) {
		super(data, uri);
	}

	public DataResource(Map<String, DataValue> dataMap) {
		this.dataMap = dataMap;
	} 

	public Map<String, DataValue> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, DataValue> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------------------------------");
		sb.append(lineSeparator);
		sb.append("DataResource");
		sb.append(lineSeparator);
		sb.append(super.toString());
		sb.append("DataMap : { ");
		sb.append(lineSeparator);
		Map<String, DataValue> dataMap = getDataMap();
		for (Entry<String, DataValue> entry : dataMap.entrySet()) {
			sb.append("Port '" + entry.getKey() + "' = '" + entry.getValue() + "'");
			sb.append(lineSeparator);			
		}
		sb.append("}");
		sb.append(lineSeparator);
		sb.append("---------------------------------------");
		sb.append(lineSeparator);
		return sb.toString();
	}
	
}
