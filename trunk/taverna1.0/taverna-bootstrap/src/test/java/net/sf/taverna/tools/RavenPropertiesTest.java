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
 * Filename           $RCSfile: RavenPropertiesTest.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-25 13:51:40 $
 *               by   $Author: sowen70 $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.tools;

import java.util.Properties;

import junit.framework.TestCase;

public class RavenPropertiesTest extends TestCase {	
	
	String realTavHome;
	
	@Override
	protected void setUp() throws Exception {
		realTavHome=System.getProperty("taverna.home");
		String resourcePath = RavenPropertiesTest.class.getResource("/conf/raven.properties").toExternalForm();
		resourcePath=resourcePath.replaceAll("file:","");
		resourcePath=resourcePath.replaceAll("conf/raven.properties", "");
		System.out.println("Looking for conf/raven.properties in: "+resourcePath);
		System.setProperty("taverna.home", resourcePath);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (realTavHome!=null)
			System.setProperty("taverna.home", realTavHome);
		else
			System.clearProperty("taverna.home");
		RavenProperties.getInstance().flush();
		System.clearProperty("raven.profilelist");
		System.clearProperty("raven.profile");
	}

	public void testRavenProperties() throws Exception {
		
		System.setProperty("raven.splashscreen","a splashscreen");
		
		Properties props = RavenProperties.getInstance().getProperties();		
		
		assertNotNull("No raven.loader.groupid defined",props.getProperty("raven.loader.groupid"));
		assertNotNull("No raven.loader.artifactid defined",props.getProperty("raven.loader.artifactid"));
		assertNotNull("No raven.loader.version defined",props.getProperty("raven.loader.version"));
		assertNotNull("No raven.loader.class defined",props.getProperty("raven.loader.class"));
		assertNotNull("No raven.loader.method defined",props.getProperty("raven.loader.method"));
		assertNotNull("No raven.repository.11 defined",props.getProperty("raven.repository.11"));		
		assertNotNull("No raven.splashscreen defined",props.getProperty("raven.splashscreen"));
		assertNotNull("No raven.splashscreen.timeout defined",props.getProperty("raven.splashscreen.timeout"));		
		assertNotNull("No raven.target.groupid defined",props.getProperty("raven.target.groupid"));
		assertNotNull("No raven.target.artifactid defined",props.getProperty("raven.target.artifactid"));
		assertNotNull("No raven.target.version defined",props.getProperty("raven.target.version"));
		assertNotNull("No raven.target.class defined",props.getProperty("raven.target.class"));
		assertNotNull("No raven.target.method defined",props.getProperty("raven.target.method"));
		
		//test System overide
		assertEquals("overidden value should be 'a splashscreen'","a splashscreen",props.getProperty("raven.splashscreen"));		
	}
	
	public void testAvailableForUpdatesTrue() {
		System.setProperty("raven.profilelist",ProfileSelectorTest.PROFILE_BASE_URL+"test-profilelist.xml");
		assertTrue("updates should be allowed",RavenProperties.getInstance().configuredForUpdates());
	}
	
	public void testAvailableForUpdatesFalse() {
		RavenProperties.getInstance().getProperties().remove("raven.profilelist");
		assertFalse("updates should not be be available", RavenProperties.getInstance().configuredForUpdates());
	}
	
	public void testProfileMirrorList() {
		System.setProperty("raven.profile", "http://somedodgyurl.com/profile.xml "+ProfileSelectorTest.PROFILE_BASE_URL+"taverna-1.5.0.0-profile.xml");
		String profile = RavenProperties.getInstance().getRavenProfileLocation();
		
		assertEquals("List should have been resolved down to 1 that works",profile,ProfileSelectorTest.PROFILE_BASE_URL+"taverna-1.5.0.0-profile.xml");
	}
	
	public void testProfileListMirrorList() {
		System.setProperty("raven.profilelist", "http://somedodgyurl.com/profilelist.xml "+ProfileSelectorTest.PROFILE_BASE_URL+"test-profilelist.xml");
		String profileList = RavenProperties.getInstance().getRavenProfileListLocation();
		
		assertEquals("list should have been resolved down to 1 that works",profileList,ProfileSelectorTest.PROFILE_BASE_URL+"test-profilelist.xml");
	}
}
