/*
 * Copyright (C) 2006-2007 The University of Manchester 
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
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-08-08 16:56:35 $
 *               by   $Author: sowen70 $
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
	private ProfileFactory() {
	}

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
	
	public boolean isProfileDefined() {
		return System.getProperty("raven.profile")!=null;
	}

	/**
	 * Get the current system profile as specified by 
	 * system property <code>raven.profile</code>. Subsequent
	 * calls will return the same instance.
	 * 
	 * @return Global {@link Profile} instance, or null if an error occured
	 */
	public Profile getProfile() {
		if (profile != null) {
			return profile;
		}
		if (!isProfileDefined()) {
			logger.warn("No profile defined, try specifying -Draven.profile");
			return null;
		}
		String profileStr = System.getProperty("raven.profile");
		try {
			URL profileURL = new URL(profileStr);
			profile = new Profile(profileURL.openStream(), true);
			return profile;
		} catch (Exception e) {
			logger.warn("Could not fetch profile from: " + profileStr
				+ " using stored profile.", e);
			return null;
		}
	}

}
