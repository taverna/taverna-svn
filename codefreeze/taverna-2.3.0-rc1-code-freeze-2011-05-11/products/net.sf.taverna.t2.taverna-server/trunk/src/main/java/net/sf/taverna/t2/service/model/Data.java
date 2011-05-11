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
package net.sf.taverna.t2.service.model;

import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;

/**
 * A Data object holds input or output data for a workflow.
 *
 * @author David Withers
 */
public class Data extends IdentifiableImpl {
	
	private Map<String, T2Reference> dataMap;

	/**
	 * Returns a map of port name to T2Reference.
	 *
	 * @return a map of port name to T2Reference
	 */
	public Map<String, T2Reference> getReferenceMap() {
		return dataMap;
	}

	/**
	 * Sets the map of port name to T2Reference.
	 *
	 * @param dataMap the new map of port name to T2Reference
	 */
	public void setReferenceMap(Map<String, T2Reference> dataMap) {
		this.dataMap = dataMap;
	}

}
