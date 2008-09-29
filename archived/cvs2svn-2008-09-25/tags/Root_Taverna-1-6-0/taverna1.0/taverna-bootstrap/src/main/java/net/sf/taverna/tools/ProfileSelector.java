package net.sf.taverna.tools;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Handles the selection of a profile, determined by the properties $raven.profile and $raven.profilelist.
 * The profile that gets used is determined by the following rules
 * 
 * $raven.profilelist - Is only used if $raven.profile is not defined. 
 * 						Contains a list of available profiles. 
 * 						On first run, Taverna will use the lowest version profile (or should it start with the latest version?) in the list and makes a local copy to $taverna.home/conf/current-profile-1.5.2.xml. 
 * 						On subsequent runs will always use the local copy. If a higher version becomes available in the list then an option to update becomes available, and if selected updates the local copy. Likewise, switching to a lower version updates the local copy. 
 * 						This is the standard configuration for Taverna releases.
 *
 * $raven.profile - if set overrides $raven.profilelist (and removes the property if it exists). and forces this profile to be used (i.e. no update request required from user). 
 * 					No local copy gets stored or read. 
 * 					This defaults to $taverna.startup/conf/profile.xml if the file exists and $raven.profile is not already defined. (so in effect $raven.profilelist is ignored if this file is present). 
 * 					This is useful for training courses, snapshot releases and custom installs.
 * 
 * Ultimately the property $raven.profile will hold the actual profile to be used
 * 
 * @author Stuart Owen
 *
 */
public class ProfileSelector {
	
	private Properties properties;
	public static final String CURRENT_PROFILE="current-profile-1.5.2.xml";
	private final String DEFAULT_RAVEN_PROFILE="profile.xml";
	
	public ProfileSelector(Properties properties) {
		this.properties=properties;
		
		String ravenProfile = properties.getProperty("raven.profile");
		String ravenProfileList = properties.getProperty("raven.profilelist");
		
		//if the profile or profilelist is defined as a space seperated list, then use the first URL that can be opened, and update the property to use this.
		if (ravenProfile!=null && ravenProfile.contains(" ")) {
			ravenProfile=selectURLFromList(ravenProfile);
			properties.setProperty("raven.profile", ravenProfile);
		}
		if (ravenProfileList!=null && ravenProfileList.contains(" ")) {
			ravenProfileList=selectURLFromList(ravenProfileList);
			properties.setProperty("raven.profilelist", ravenProfileList);
		}
		
		resolve(ravenProfile,ravenProfileList);
	}
	
	private String selectURLFromList(String urlList) {
		String [] urls = urlList.split(" ");
		String result = urls[0]; //default to the first
		boolean found=false;
		for (String urlStr : urls) {
			try {
				URL url = new URL(urlStr);
				InputStream stream = url.openStream();
				stream.close();
				result = urlStr;
				found = true;
				break;
			}
			catch(Exception e) {
				System.out.println("There is a problem connecting to the url:"+urlStr);
			}
		}
		if (!found) {
			System.out.println("Unable to connect to any of the mirror sites for to check for updated profiles.");
			System.out.println("THIS STRONGLY INDICATES YOU HAVE NO NETWORK ACCESS.");
		}
		
		return result;
	}
	
	private void resolve(String ravenProfile, String ravenProfileList) {
		if (ravenProfile==null) {
			if (!checkForProfileInStartUp()) {
				if (ravenProfileList!=null) {
					determineStartProfile(ravenProfileList);
				}
				else {
					selectCurrentIfAvailable();
				}
			}
			else {
				properties.remove("raven.profilelist");
			}
		}
		else {
			properties.remove("raven.profilelist");
		}
	}
	
	private boolean checkForProfileInStartUp() {
		boolean result = false;
		File defaultProfile = getDefaultProfileFile();
		if (defaultProfile!=null && defaultProfile.exists()) {
			String url = defaultProfile.toURI().toString();
			properties.setProperty("raven.profile", url);
			result=true;
		}
		return result;
	}
	
	private void determineStartProfile(String ravenProfileList) {
		if (ravenProfileList!=null) {
			if (!selectCurrentIfAvailable()) {
				try {
					File currentProfileFile = getCurrentProfileFile();
					if (currentProfileFile!=null) {
						URL listURL = new URL(ravenProfileList);
						ProfileListSelector listSelector = new ProfileListSelector(listURL);
						listSelector.storeFirst(currentProfileFile);
						
						//it should be available now
						if (!selectCurrentIfAvailable()) {
							System.out.println("Not able to determine current profile");
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getTavernaStartup() {
		return System.getProperty("taverna.startup");
	}
	
	private String getTavernaHome() {
		return System.getProperty("taverna.home");
	}
	
	private boolean selectCurrentIfAvailable() {
		boolean result=false;
		File current = getCurrentProfileFile();
		if (current!=null && current.exists()) {
			String url = current.toURI().toString();
			properties.setProperty("raven.profile", url);
			result = true;
		}
		return result;
	}
	
	private File getDefaultProfileFile() {
		File result = null;
		if (getTavernaStartup()!=null) {
			result = new File(getTavernaStartup(),"conf");
			result = new File(result,DEFAULT_RAVEN_PROFILE);
		}
		return result;
	}
	
	private File getCurrentProfileFile() {
		File result = null;
		if (getTavernaHome()!=null) {
			result = new File(getTavernaHome(),"conf");
			result = new File(result,CURRENT_PROFILE);
		}
		return result;
	}
	
	public String getProfileLocation() {
		return properties.getProperty("raven.profile");
	}
	
}
