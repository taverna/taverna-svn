package net.sf.taverna.raven.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.sf.taverna.raven.spi.InvalidProfileException;
import net.sf.taverna.raven.spi.Profile;

import org.apache.log4j.Logger;

/**
 * Handles checking if a new version of of a profile is available, and the copying of the remote profile
 * version to the current if its is selected.
 * 
 * @author Stuart Owen
 *
 */

public class ProfileUpdateHandler {
	private static Logger logger = Logger.getLogger(ProfileUpdateHandler.class);
	
	private URL profileListURL;
	private URL currentProfileURL;
	private boolean newVersionAvailable=false;
	
	/**
	 * Processes the profiles and profile list at the URLS and determines if an updated profile is
	 * available.
	 * @param profileListURL - URL to the list of available profiles
	 * @param currentProfileURL - URL to the local profile currently in use
	 */
	public ProfileUpdateHandler(URL profileListURL, URL currentProfileURL)  {
		this.profileListURL=profileListURL;
		this.currentProfileURL=currentProfileURL;
		try {
			checkForUpdate();
		}
		catch(Exception e) {
			logger.error("Unable to check for an updated Taverna version");
			newVersionAvailable=false;
		}
	}
	
	/**
	 * Copies the latest profile to the local file
	 * @param localFile - File for the local profile
	 *
	 * @throws Exception
	 */
	public void updateLocalProfile(File localFile) throws Exception {
		Profile localProfile = new Profile(currentProfileURL.openStream(),true);
		backupLocalProfile(localFile, localProfile);
		storeLatestProfile(localFile);
	}
	
	/**
	 * Updates the local profile to that specified by newVersion
	 * @param newVersion
	 * @param localFile
	 * @throws Exception
	 */
	public void updateLocalProfile(ProfileVersion newVersion,File localFile) throws Exception {
		Profile localProfile = new Profile(currentProfileURL.openStream(),true);
		backupLocalProfile(localFile, localProfile);
		storeProfileToLocal(newVersion, localFile);
	}
	
	private void storeLatestProfile(File localFile) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, InvalidProfileException, MalformedURLException, IOException {
		ProfileVersion latestVersion = getLatestRemoteProfile();
		storeProfileToLocal(latestVersion, localFile);
	}
	
	private void storeProfileToLocal(ProfileVersion newVersion, File localFile) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, InvalidProfileException, MalformedURLException, IOException {
		Profile latestRemote = new Profile(new URL(newVersion.profileLocation).openStream(),true);
		latestRemote.write(new FileOutputStream(localFile));
	}
	
	private ProfileVersion getLatestRemoteProfile() {
		List<ProfileVersion> versions = ProfileVersions.getProfileVersions(profileListURL);
		ProfileVersion latest = versions.get(versions.size()-1);
		return latest;
	}
	
	private void backupLocalProfile(File localProfileFile, Profile localProfile) throws Exception {
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
	
	private void checkForUpdate() throws IOException, InvalidProfileException {
		Profile localProfile = new Profile(currentProfileURL.openStream(),true);
		List<ProfileVersion> versions = ProfileVersions.getProfileVersions(profileListURL);
		
		String currentVersion = localProfile.getVersion();
		newVersionAvailable=(versions.get(versions.size()-1).version.compareTo(currentVersion)>0);
	}
	
	public boolean isNewVersionAvailable() {
		return newVersionAvailable;
	}
}
