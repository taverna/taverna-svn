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
package net.sf.taverna.raven.appconfig.bootstrap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Class responsible for determining the raven repositories during the Bootsrap process
 *
 * @author Stuart Owen
 */
public class Repositories {
//
//	public URL [] find() {
//		Properties properties = RavenProperties.getInstance().getProperties();
////		 entries are named raven.repository.2 = http:// ..
//		// We'll add these in order as stated (not as in property file)
//		String prefix = "raven.repository.";
//		ArrayList<URL> urls = new ArrayList<URL>();
//
//		//adds $taverna.startup/repository to start of the list if it exists.
//		URL startupURL = findStartupURL(properties);
//		if (startupURL!=null) {
//	        urls.add(startupURL);
//        }
//
//		//detect if an old repositorys from 1.5.1 onwards exist, and if so add them to the list of available repositories.
//		//this reduces duplication between the 2 repositories.
//		String [] applications=new String[]{"Taverna","Taverna-1.5.2","Taverna-1.6.0","Taverna-1.6.1","Taverna-1.6.2","Taverna-1.7.0"};
//		for (String application : applications) {
//			URL oldRespository = findOldRepository(application);
//			if (oldRespository!=null) {
//	            urls.add(oldRespository);
//            }
//		}
//
//		for (Entry property : properties.entrySet()) {
//			String propName = (String) property.getKey();
//			if (!propName.startsWith(prefix)) {
//				continue;
//			}
//			String propValue = (String) property.getValue();
//			URL url;
//			try {
//				url = new URL(propValue);
//			} catch (MalformedURLException e1) {
//				System.err.println("Ignoring invalid URL " + propValue);
//				continue;
//			}
//			int position;
//			try {
//				position = Integer.valueOf(propName.replace(prefix, ""));
//			} catch (NumberFormatException e) {
//				// Just ignore the position
//				System.err.println("Invalid URL position " + propName);
//				urls.add(url);
//				continue;
//			}
//			// Fill up with null's if we are to insert way out there
//			while (position >= urls.size()) {
//				urls.add(null);
//			}
//			// .add(pos, url) makes sure we don't overwrite anything
//			urls.add(position, url);
//		}
//
//		// Remove nulls and export as URL[]
//		while (urls.remove(null)) {
//			// nothing
//		}
//		return urls.toArray(new URL[0]);
//	}
//
//	/**
//	 * Returns the url for the startup repsitory ($taverna.startup/repository) if it is defined and exists.
//	 * Otherwise returns null
//	 * @param properties
//	 * @return
//	 */
//	private URL findStartupURL(Properties properties) {
//		URL result = null;
//		String startup=properties.getProperty("taverna.startup");
//		if (startup!=null) {
//			File repository = new File(startup,"repository");
//			if (repository.exists()) {
//	            try {
//					result = repository.toURI().toURL();
//				} catch (MalformedURLException e) {
//					System.out.println("Malformed URL exception whilst determining startup repository ($taverna.startup/repository/");
//					e.printStackTrace();
//				}
//            }
//		}
//		return result;
//	}
//
//	/**
//	 * Checks for the old default taverna.home location, and if it exists and contains a 'repository' directory
//	 * then returns a URL to this for use an artifact repository.
//	 * @return
//	 */
//	private URL findOldRepository(String application) {
//		File appHome=null;
//		URL result = null;
//
//		File home = new File(System.getProperty("user.home"));
//		if (home.isDirectory()) {
//			String os = System.getProperty("os.name");
//			if (os.equals("Mac OS X")) {
//				File libDir = new File(home, "Library/Application Support");
//				appHome = new File(libDir, application);
//			} else if (os.startsWith("Windows")) {
//				String APPDATA = System.getenv("APPDATA");
//				File appData = null;
//				if (APPDATA != null) {
//					appData = new File(APPDATA);
//				}
//				if (appData != null && appData.isDirectory()) {
//					appHome = new File(appData, application);
//				} else {
//					appHome = new File(home, application);
//				}
//			} else {
//				// We'll assume UNIX style is OK
//				appHome = new File(home, "." + application.toLowerCase());
//			}
//		}
//
//		if (appHome!=null && appHome.exists()) {
//			File repository = new File(appHome, "repository");
//			if (repository.exists()) {
//				try {
//					result=repository.toURL();
//				} catch (MalformedURLException e) {
//					System.out.println("There was an error finding repositories of previous Taverna installations");
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return result;
//	}

}
