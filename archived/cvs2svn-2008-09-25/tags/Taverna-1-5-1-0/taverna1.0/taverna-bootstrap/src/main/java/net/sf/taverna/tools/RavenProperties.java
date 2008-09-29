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
 * Filename           $RCSfile: RavenProperties.java,v $
 * Revision           $Revision: 1.7.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-23 15:43:58 $
 *               by   $Author: sowen70 $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Class to handle the raven.properties. Upon construction it first checks for a remote copy, which if found it downloads and stores locally.
 * If it fails to get a remote copy it uses a locally stored copy if present, otherwise as a last resort it uses
 * the copy bundled with the bootstrap jar.
 * 
 * @author Stuart Owen
 *
 */

@SuppressWarnings("serial")
public class RavenProperties extends Properties {

	public enum RavenPropertiesSource {
		LOCAL, REMOTE, RESOURCE, USERDEFINED;
	}

	private RavenPropertiesSource source;

	public RavenPropertiesSource getSource() {
		return source;
	}

	/**
	 * 
	 * @param localfile the file to check for the locally stored properties, and also to store the current copy of the remote version. If null, then the local copy is not checked or stored.
	 * @throws Exception
	 */
	public RavenProperties(File localfile) throws Exception {
		super();
		if (!findRemoteProperties()) {
			if (localfile == null || !findLocalProperties(localfile)) {
				if (!findResourceProperties()) {
					throw new Exception("Unable to find raven properties");
				}
			}
		} else {
			if (localfile != null) {
				saveLocalCopy(localfile);
			}
		}
		// Allow overriding any of those on command line
		putAll(System.getProperties());
	}

	protected boolean findRemoteProperties() {	
		boolean found = false;
		String userLocation=(System.getProperty("raven.properties"));
		if (userLocation==null) {
			String[] locations = Bootstrap.REMOTE_PROPERTIES.split(",");
			
			for (String location : locations) {
				try {
					URL propUrl = new URL(location);
					URLConnection con=propUrl.openConnection();
					
					//give it a 5 second timeout so that the user isn't left hanging around if the server is down.
					con.setConnectTimeout(5000);				
					load(propUrl.openStream());
					found = true;
					source = RavenPropertiesSource.REMOTE;
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			System.out.println("Using raven.propeties location of:"+userLocation);
			try {
				URL propUrl=new URL(userLocation);
				load(propUrl.openStream());
				found=true;
				source=RavenPropertiesSource.USERDEFINED;
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
		return found;
	}

	public void saveLocalCopy(File localcopy) {
		try {
			store(new FileOutputStream(localcopy), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean findLocalProperties(File localcopy) {
		boolean found = false;
		try {
			load(localcopy.toURI().toURL().openStream());
			source = RavenPropertiesSource.LOCAL;
			found = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return found;
	}

	protected boolean findResourceProperties() {
		boolean found = false;
		String propsName = "/raven.properties";
		InputStream propStream = RavenProperties.class
				.getResourceAsStream(propsName);
		if (propStream == null) {
			System.err.println("Could not find " + propsName);
			System.exit(1);
		}
		try {
			load(propStream);
			found = true;
		} catch (IOException e) {
			System.err.println("Could not load " + propsName);
			System.exit(2);
		}

		return found;
	}

}
