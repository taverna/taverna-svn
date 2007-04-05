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
 * Filename           $RCSfile: ProxyConfig.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 15:46:09 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration;

import java.net.URL;
import java.util.List;

import uk.org.mygrid.dataproxy.configuration.impl.WSDLConfigException;

public interface ProxyConfig {
		
	public String getContextPath();
	public void setContextPath(String contextPath);
	
	//FIXME: is this more useful as a string rather than a URL???
	public URL getStoreBaseURL();
	public void setStoreBaseURL(URL storeBaseURL);
	public WSDLConfig getWSDLConfigForID(String ID);
	public void addWSDLConfig(WSDLConfig config) throws WSDLConfigException;
	public void deleteWSDLConfig(WSDLConfig config);
	public List<WSDLConfig> getWSDLConfigs();
	
	public String toStringForm();
}
