
/*
 * Copyright (C) 2003 The University of Chicago 
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
 * Filename           $RCSfile: GT4Service.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-01-05 14:54:32 $
 *               by   $Author: tanwei $
 * Created on 01-Dec-2007
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.gt4;

import java.util.ArrayList;
import java.util.List;

public class GT4Service {
	
	private String wsdlLocation;
	
	private String serviceName;
	private List<String> operations = new ArrayList<String>();
	
	
	public GT4Service(String location, String name){
		this.wsdlLocation = location;
		this.serviceName = name;
	}
	
	
	public boolean addOperation(String s) {
		return operations.add(s);
	}

	public String getServiceName() {
		return serviceName;
	}
	public String getServiceWSDLLocation(){
		return wsdlLocation;
	}

	public List<String> getOperations() {
		return operations;
	}
	
}
