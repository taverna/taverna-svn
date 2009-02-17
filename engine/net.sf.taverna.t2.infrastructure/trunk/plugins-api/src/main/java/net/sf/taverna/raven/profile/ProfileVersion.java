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
 * Filename           $RCSfile: ProfileVersion.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:52:03 $
 *               by   $Author: sowen70 $
 * Created on 16 Jan 2007
 *****************************************************************/
package net.sf.taverna.raven.profile;

import java.net.MalformedURLException;
import java.net.URL;

import org.jdom.Element;

public class ProfileVersion {
	String version="";
	String name="";
	String profileLocation="";
	String description="";
	
	public String getName() {
		return name;
	}
	
	public String getProfileLocation() {
		return profileLocation;
	}
	
	public String getVersion() {
		return version;
	}
		
	public String getDescription() {
		return description;
	}
	
	public static ProfileVersion fromXml(Element element, URL sourceURL) throws MalformedURLException {
		ProfileVersion result = new ProfileVersion();
		if (element.getChild("version")!=null) result.version=element.getChildTextTrim("version");
		if (element.getChild("name")!=null) result.name=element.getChildTextTrim("name");
		if (element.getChild("location")!=null) {
			result.profileLocation=element.getChildTextTrim("location");					
			if (sourceURL!=null) {
				URL correctURL=new URL(sourceURL,result.profileLocation);
				result.profileLocation=correctURL.toString();
			}
		}		
		if (element.getChild("description")!=null) result.description = element.getChildTextTrim("description");			
		return result;		
	}
}
