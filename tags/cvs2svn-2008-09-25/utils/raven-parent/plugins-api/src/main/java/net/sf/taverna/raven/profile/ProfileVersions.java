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
 * Filename           $RCSfile: ProfileVersions.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-09-04 14:52:03 $
 *               by   $Author: sowen70 $
 * Created on 16 Jan 2007
 *****************************************************************/
package net.sf.taverna.raven.profile;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Reads a profile list from an XML document to retreive a list of ProfileVersion
 * XML should be of the form:
 * <code>
 * <profileversions>
 * 		<profile>
 * 			<version>1.5.0</version>
 * 			<name>A Profile</name>
 * 			<location>http://url to profile</location>
 * 		</profile>
 * <profileversions>
 * </code>
 * @author Stuart Owen
 *
 */

public class ProfileVersions {
	
	private static Logger logger = Logger.getLogger(ProfileVersions.class);	
	
	public static List<ProfileVersion> getProfileVersions(URL profileListUrl) {
		try {						
			InputStream str=profileListUrl.openStream();
			return getProfileVersions(str,profileListUrl);
		}
		catch(Exception e) {
			logger.error("Error opening the stream to the URL:"+profileListUrl.toString(),e);
		}
		return new ArrayList<ProfileVersion>();
	}
	
	/**
	 * Provides a list of available profiles read from a stream, provided by an external xml source (usually http hosted).
	 * If the sourceURL is not null then this can be used to resolve the partial URL's of profile locations based on the assumption that they are hosted
	 * at the same place.  
	 * 
	 * The list is ordered according to the version number, ascending
	 * 
	 * @param profileListStream the stream to the profile versions XML document
	 * @param sourceURL if not null, then this URL is used to resolve partial URL's within the profile version XML document to their full path
	 * @return
	 */
	public static List<ProfileVersion> getProfileVersions(InputStream profileListStream, URL sourceURL) {
		List<ProfileVersion> result = new ArrayList<ProfileVersion>();				
		
		try {			
			SAXBuilder builder = new SAXBuilder();
			Document doc=builder.build(profileListStream);
			List<Element> profiles = doc.getRootElement().getChildren("profile");
			for (Element profileElement : profiles) {
				try {
					result.add(ProfileVersion.fromXml(profileElement, sourceURL));
				}
				catch(MalformedURLException e) {
					logger.error("An error occurred processing the URL of the profile definition",e);
				}
			}
			
		}
		catch(Exception e) {
			logger.error("Error reading xml for the list of available profile versions from "+profileListStream.toString(),e);
		}
		
		sortList(result);
		
		return result;
	}
	
	private static void sortList(List<ProfileVersion> list) {
		Collections.sort(list,new Comparator<ProfileVersion>() {
			public int compare(ProfileVersion v1, ProfileVersion v2) {
				return v1.version.compareTo(v2.version);
			}
		});
	}
	
}
