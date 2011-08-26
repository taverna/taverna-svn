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
 * Filename           $RCSfile: ProfileHandler.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-15 10:14:44 $
 *               by   $Author: sowen70 $
 * Created on 25 Oct 2006
 *****************************************************************/
package net.sf.taverna.update;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import net.sf.taverna.raven.spi.Profile;

import org.apache.log4j.Logger;

/**
 * Handler to detect for new profile versions, and update local copy of the profile to the new one if requested
 * dependencies
 * @author Stuart Owen
 *
 */
public class ProfileHandler {
	
	private static Logger logger = Logger.getLogger(ProfileHandler.class);
	
	private URL remoteProfileURL;	
	private Profile remoteProfile;
	private Profile localProfile;
	private boolean newVersionAvailable=false;
	
	public ProfileHandler(String remoteProfileLocation) throws Exception {
		this.remoteProfileURL=new URL(remoteProfileLocation);
		this.remoteProfile=new Profile(remoteProfileURL.toURI().toURL().openStream(),true);
		init();
	}
		
	public boolean isNewVersionAvailable() {
		return newVersionAvailable;
	}
	
	private void init() throws Exception {
		File localProfileFile = getLocalProfileFile();
		if (localProfileFile.exists() && remoteProfile!=null) {
			localProfile=new Profile(localProfileFile.toURI().toURL().openStream(),true);			
			if (remoteProfile.getVersion().compareTo(localProfile.getVersion())>0) {
				newVersionAvailable=true;
			}
		}
	}
	
	private File getLocalProfileFile() {
		String profileStr=remoteProfileURL.getPath();
		File tavernaHome=new File(System.getProperty("taverna.home"));
		File userdir=new File(tavernaHome,"conf");
		String fileStr=profileStr;
		if (fileStr.contains("/")) {
			int i=fileStr.lastIndexOf("/");
			fileStr=fileStr.substring(i+1);
		}
		File profileFile=new File(userdir,fileStr);
		return profileFile;
	}	
	
	public void updateLocalProfile() throws Exception {
		File localProfileFile=getLocalProfileFile();
		backupLocalProfile(localProfileFile);
		storeLocalProfile(localProfileFile);
	}
	
	private void backupLocalProfile(File localProfileFile) throws Exception {
		if (localProfile!=null) {
			File backupfile=new File(localProfileFile.getPath()+"-"+localProfile.getVersion()+".bak");
			try {
				localProfile.write(new FileOutputStream(backupfile));
			} catch(Exception e) {
				logger.error("Error backing up current profile",e);
				throw e;
			}
		}
	}
	
	
	private void storeLocalProfile(File localProfileFile) throws Exception {
		try {
			remoteProfile.write(new FileOutputStream(localProfileFile));
		}
		catch(Exception e) {
			logger.error("Error updating profile",e);
			//revert back to old profile and throw back exception
			try {
				localProfile.write(new FileOutputStream(localProfileFile));
			}
			catch(Exception ex) {
				logger.error("Error rewriting last profile",ex);
			}
			throw e;
		}
	}
}
