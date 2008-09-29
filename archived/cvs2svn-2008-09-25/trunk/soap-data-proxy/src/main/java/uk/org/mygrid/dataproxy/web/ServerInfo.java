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
 * Filename           $RCSfile: ServerInfo.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 12 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.wings.session.SessionManager;

/**
 * Provides some basic information about the running server.
 * 
 * @author Stuart Owen
 */

public class ServerInfo {
	
	private static Logger logger = Logger.getLogger(ServerInfo.class);	
	private static String location = null;
	private static ServletContext context=null;
	
	/**
	 * Provides the location of the config.xml file that proxy configuration information is stored.
	 * This either declared as an init parameter in the web.xml, or if missing there is stored the
	 * root of the web-app (this is not advisable, but is secured against viewing in the web.xml).
	 * 
	 * @return
	 */
	public static String getConfigFileLocation() {
		if (location == null) {			
			location=locationParam();
			if (location==null) {	
				ServletContext c = getServletContext();
				if (c!=null) {
					location=c.getRealPath("config.xml");
					logger.warn("Storing config within web app context!\n Modify the ConfigFileLocation parameter in the web.xml to specify the location that the config file should be stored.");
					logger.warn("This location is:"+location);
				}							
			}
			else {
				logger.info("Location for configuration file defined as:"+location);
			}
		}
		return location;
	}
	
	private static String locationParam() {
		String result = null;
		ServletContext c=getServletContext();
		if (c!=null) {
			result=getServletContext().getInitParameter("ConfigFileLocation");
		}
		
		return result;
	}
	
	/**
	 * Sets the ServletContext, which is used to examine servlet paremeters declared in the web.xml
	 * @param context
	 */
	public static void setServletContext(ServletContext context) {
		ServerInfo.context=context;
	}
	
	/**
	 * @return the ServletContext
	 */
	public static ServletContext getServletContext() {
		if (context==null && SessionManager.getSession()!=null) {
			context=SessionManager.getSession().getServletContext();
		}
		return context;
	}
}
