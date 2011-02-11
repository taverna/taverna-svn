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
package net.sf.taverna.t2.activities.wsdl;

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * A standard Java Bean that provides the details required to configure a WSDLActivity.
 * <p>
 * This contains details about the WSDL and the Operation that the WSDLActivity is intended to invoke.
 * </p>
 * @author Stuart Owen
 */
@ConfigurationBean(uri = WSDLActivity.URI + "/configuration")
public class WSDLActivityConfigurationBean {
    private WSDLOperationConfigurationBean operation;
    private String securityProfile;
    
    // In the case service requires username and password for authentication,
    // but do not serialise these variables to file
    //transient private String username;
    //transient private String password;
    
    /** Creates a new instance of WSDLActivityConfigurationBean */
    public WSDLActivityConfigurationBean() {
    }

    public WSDLOperationConfigurationBean getOperation() {
        return operation;
    }

	@ConfigurationProperty(name = "operation", label = "WSDL Operation", description = "The WSDL operation")
    public void setOperation(WSDLOperationConfigurationBean operation) {
        this.operation = operation;
    }

	public String getSecurityProfile() {
		return securityProfile;
	}

	@ConfigurationProperty(name = "securityProfile", label = "Security Profile", description = "WS-Security settings required by the web service", required = false)
	public void setSecurityProfile(String securityProfile) {
		this.securityProfile = securityProfile;
	}

//	public void setUsername(String username) {
//		this.username = username;
//	}
//
//	public String getUsername() {
//		return username;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	public String getPassword() {
//		return password;
//	}
}
