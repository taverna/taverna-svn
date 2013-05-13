/*******************************************************************************
 * Copyright (C) 2009-2010 The University of Manchester
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
package net.sf.taverna.raven.plugins.ui;

import java.io.File;
import java.util.Date;

import uk.org.taverna.commons.plugin.PluginManager;
import uk.org.taverna.configuration.app.ApplicationConfiguration;

import net.sf.taverna.t2.workbench.StartupSPI;

/**
 * Startup hook for checking if there are available updates for Taverna plugins.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class CheckForUpdatesStartupHook implements StartupSPI {

	public static final String CHECK_FOR_UPDATES_DIRECTORY_NAME = "updates";
	public static final String LAST_UPDATE_CHECK_FILE_NAME = "last_update_check";

	private PluginManager pluginManager;
	private ApplicationConfiguration applicationConfiguration;

	public static File checkForUpdatesDirectory = getCheckForUpdatesDirectory();
	public static File lastUpdateCheckFile = new File(checkForUpdatesDirectory,
			LAST_UPDATE_CHECK_FILE_NAME);

	public int positionHint() {
		return 90;
	}

	public boolean startup() {

		// Check if more than 2 weeks passed since we checked for updates.
		if (lastUpdateCheckFile.exists()) {
			long lastModified = lastUpdateCheckFile.lastModified();
			long now = new Date().getTime();

			if (now - lastModified < 14 * 24 * 3600 * 1000) { // 2 weeks have not passed since we
																// last asked
				return true;
			} else { // Check again for updates
				if (pluginManager.checkForUpdates()) {
					CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
					dialog.setVisible(true);
				}
				return true;
			}
		} else {
			// If we are here - then this is the first time to check for updates
			if (pluginManager.checkForUpdates()) {
				CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
				dialog.setVisible(true);
			}
			return true;
		}
	}

	/**
	 * Gets the registration directory where info about registration will be saved to.
	 */
	public File getCheckForUpdatesDirectory() {

		File home = applicationConfiguration.getApplicationHomeDir();

		File registrationDirectory = new File(home, CHECK_FOR_UPDATES_DIRECTORY_NAME);
		if (!registrationDirectory.exists()) {
			registrationDirectory.mkdir();
		}
		return registrationDirectory;
	}
}
