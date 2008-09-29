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
 * Filename           $RCSfile: UpdatesAvailableIcon.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-13 13:12:49 $
 *               by   $Author: sowen70 $
 * Created on 12 Dec 2006
 *****************************************************************/
package net.sf.taverna.update.plugin.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.taverna.update.ProfileHandler;
import net.sf.taverna.update.plugin.PluginManager;
import net.sf.taverna.update.plugin.event.PluginManagerEvent;
import net.sf.taverna.update.plugin.event.PluginManagerListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * A JLabel that periodically checks for updates, running on a daemon thread. If updates are available it makes
 * itself visible and responds to click events to display the appropriate update
 * response.
 * 
 * Also acts as a pluginmanager listener to refresh itself whenever a new plugin is added.
 * 
 * @author Stuart Owen
 *
 */

@SuppressWarnings("serial")
public class UpdatesAvailableIcon extends JLabel implements PluginManagerListener {
	
	private UpdateProfileMouseAdaptor updateProfileMouseAdaptor=new UpdateProfileMouseAdaptor();
	private UpdatePluginsMouseAdaptor updatePluginMouseAdaptor=new UpdatePluginsMouseAdaptor();	
	private static Logger logger = Logger.getLogger(UpdatesAvailableIcon.class);
	private static boolean profileUpdated=false;
	
	private final int CHECK_INTERVAL = 1800000; //every 30 minutes
	
	public UpdatesAvailableIcon() {
		super();		
		setVisible(false);		
		startCheckThread();
		PluginManager.getInstance().addPluginManagerListener(this);
	}
	
	

	public void pluginAdded(PluginManagerEvent event) {
		logger.info("Plugin Added");
		if (!isVisible()) checkForUpdates();		
	}

	public void pluginChanged(PluginManagerEvent event) {
				
	}

	public void pluginRemoved(PluginManagerEvent event) {
		logger.info("Plugin Removed ");
		if (isVisible()) checkForUpdates();
	}
	
	private void startCheckThread() {
		Thread checkThread = new Thread("Check for updates thread") {

			@Override
			public void run() {				
				while(true) {
					try {						
						checkForUpdates();
						Thread.sleep(CHECK_INTERVAL);
					}
					catch(InterruptedException e) {
						logger.warn("Interruption exception in checking for updates thread",e);
					}					
				}
			}			
		};
		checkThread.setDaemon(true); //daemon so that taverna will stop the thread and close on exit.
		checkThread.start();
	}
	
	private synchronized void checkForUpdates() {		
		
		if (profileUpdatesAvailable()) {
			logger.info("Profile update available");
			setToolTipText("A core taverna update is available");
			setIcon(TavernaIcons.updateRecommendedIcon);			
			setVisible(true);
			if (!Arrays.asList(getMouseListeners()).contains(updateProfileMouseAdaptor)) {
				addMouseListener(updateProfileMouseAdaptor);
			}
			return;
		}
		
		if (pluginUpdateAvailable()) {
			logger.info("Plugin update available");
			setToolTipText("A plugin update is available");
			setVisible(true);
			setIcon(TavernaIcons.updateIcon);
			if (!Arrays.asList(getMouseListeners()).contains(updatePluginMouseAdaptor)) {
				addMouseListener(updatePluginMouseAdaptor);
			}
			return;
		}
		
		setToolTipText("");
		setVisible(false);
				
	}
	
	private boolean pluginUpdateAvailable() {			
		return PluginManager.getInstance().checkForUpdates();						
	}
	
	private String getRemoteProfile() {
		return System.getProperty("raven.remoteprofile");
	}
	
	private boolean profileUpdatesAvailable() {
		if (profileUpdated) return false; //return false if a profile has been updated but Taverna has not been restarted
		boolean result=false;
		
		try {
			result=new ProfileHandler(getRemoteProfile()).isNewVersionAvailable();
		} catch (Exception e) {
			logger.error("Error checking for profile updates",e);
		}
		return result;
	}
	
	private final class UpdateProfileMouseAdaptor extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			//FIXME: this assumes the button is on the toolbar.
			Component parent=UpdatesAvailableIcon.this.getParent().getParent();
			
			int confirmation = JOptionPane.showConfirmDialog(parent,
					"New updates are available to the Taverna core, update now?",
					"New updates available", JOptionPane.YES_NO_OPTION);
			if (confirmation == JOptionPane.YES_OPTION) {
										
				try {
					new ProfileHandler(getRemoteProfile()).updateLocalProfile();
					JOptionPane.showMessageDialog(parent,
							"Your updates will be applied when you restart Taverna",
							"Restart required", JOptionPane.INFORMATION_MESSAGE);
					profileUpdated=true;
					checkForUpdates();
				} catch (Exception ex) {
					logger.error("Error updating local profile", ex);
					JOptionPane.showMessageDialog(parent,
							"Updating your profile failed, try again later.",
							"Error updating profile", JOptionPane.WARNING_MESSAGE);
				}
			}
			removeMouseListener(this);
			checkForUpdates();
		}
	}
	
	private final class UpdatePluginsMouseAdaptor extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
//			FIXME: this assumes the button is on the toolbar.
			Component parent=UpdatesAvailableIcon.this.getParent().getParent();
			
			final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(PluginManager.getInstance());
			pluginManagerUI.setLocationRelativeTo(parent);
			pluginManagerUI.setVisible(true);						
		}
		
	}
	
}
