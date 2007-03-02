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
 * Filename           $RCSfile: ProfileFactory.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-12 15:10:44 $
 *               by   $Author: stain $
 * Created on 20 Oct 2006
 *****************************************************************/
package net.sf.taverna.raven.spi;

import java.net.URL;

import net.sf.taverna.raven.log.Log;


/**
 * A factory class for getting an instance of the Profile of the Taverna
 * components according to the profile xml at the location defined by the system
 * property taverna.profile. Once loaded it stores a copy locally which is used
 * as a fallback should the defined location be innaccessible.
 */
public class ProfileFactory {
	private static Log logger = Log.getLogger(ProfileFactory.class);
	
	private static Profile profile = null;

	private static ProfileFactory instance = null;
	
	/**
	 * Don't instanciate, use singleton {@link #getInstance()}.
	 */ 
	private ProfileFactory() {}
	
	
	/**
	 * Get the singleton factory.
	 * 
	 */
	public static ProfileFactory getInstance() {
		if (instance == null) {
			instance = new ProfileFactory();
		}
		return instance;
	}
	
	/**
	 * Get the current system profile as specified by 
	 * system property <pre>raven.profile</pre>. Subsequent
	 * calls will return the same instance.
	 * 
	 * @return System {@link Profile} instance
	 */
	public Profile getProfile() {
		if (profile != null) {
			return profile;
		}
		String profileStr=System.getProperty("raven.profile");
		if (profileStr == null) {
			logger.warn("No profile defined, try specifying -Draven.profile");
			return null;
		}
		try {				
			URL profileURL = new URL(profileStr);					
			profile = new Profile(profileURL.openStream(), true);
			return profile;
		} catch (Exception e) {
			logger.warn("Could not fetch profile from: "+profileStr+" using stored profile.", e);	
			return null;
		}								
	}


	
}
