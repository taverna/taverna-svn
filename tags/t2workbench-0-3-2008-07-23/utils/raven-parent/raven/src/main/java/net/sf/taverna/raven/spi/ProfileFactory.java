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
 *****************************************************************/
package net.sf.taverna.raven.spi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
	 * Get the singleton ProfileFactory.
	 * 
	 */
	public static synchronized ProfileFactory getInstance() {
		if (instance == null) {
			instance = new ProfileFactory();
		}
		return instance;
	}

	/**
	 * Don't instantiate, use singleton {@link #getInstance()}.
	 */
	private ProfileFactory() {
	}

	/**
	 * Get the current system profile as specified by system property
	 * <code>raven.profile</code>. Subsequent calls will return the same
	 * instance.
	 * 
	 * @return Global {@link Profile} instance, or null if an error occured
	 */
	public Profile getProfile() {
		if (profile != null) {
			return profile;
		}
		if (!isProfileDefined()) {
			logger.info("No profile defined, try specifying -Draven.profile");
			profile = new Profile(false);
			return profile;
		}
		String profileStr = System.getProperty("raven.profile");
		try {
			URL profileURL = new URL(profileStr);
			profile = new Profile(profileURL.openStream(), true);
			updateWithPluginArtifacts(profile);
			return profile;
		} catch (Exception e) {
			logger.warn("Could not fetch profile from: " + profileStr
					+ " using stored profile.", e);
			return null;
		}
	}

	public boolean isProfileDefined() {
		return System.getProperty("raven.profile") != null;
	}

	private void updateProfileWithPluginsProfile(File pluginsFile,
			Profile profile2) {
		try {
			profile.addArtifactsForPlugins(pluginsFile.toURI().toURL()
					.openStream());
		} catch (MalformedURLException e) {
			logger.error("Invalid URL to plugins file:"
					+ pluginsFile.getAbsolutePath(), e);
		} catch (IOException e) {
			logger.error("Unable to ppen stream to plugins file:"
					+ pluginsFile.getAbsolutePath(), e);
		}
	}

	private void updateWithPluginArtifacts(Profile profile) {
		if (System.getProperty("taverna.home") != null) {
			File pluginsFile = new File(System.getProperty("taverna.home"),
					"plugins/plugins.xml");
			if (pluginsFile.exists()) {
				updateProfileWithPluginsProfile(pluginsFile, profile);
			} else {
				if (System.getProperty("taverna.startup") != null) {
					pluginsFile = new File(System
							.getProperty("taverna.startup"),
							"plugins/plugins.xml");
					if (pluginsFile.exists()) {
						updateProfileWithPluginsProfile(pluginsFile, profile);
					}
				}
			}
		}
	}

}
