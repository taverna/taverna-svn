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
package net.sf.taverna.t2.service;

import java.util.Collection;
import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.service.model.Data;
import net.sf.taverna.t2.service.webservice.resource.DataValue;

/**
 * Manages storage of workflow input and output data.
 *
 * @author David Withers
 */
public interface DataManager {

//	@PreAuthorize("hasRole('ROLE_USER')")
	public void addData(Data data);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public void deleteData(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Data getData(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Collection<Data> getAllData();
	
	public Data createData(Map<String, T2Reference> referenceMap);
	
	public Map<String, T2Reference> registerData(Map<String, DataValue> data);
	
	public Map<String, DataValue> dereferenceData(Map<String, T2Reference> referenceMap);

}
