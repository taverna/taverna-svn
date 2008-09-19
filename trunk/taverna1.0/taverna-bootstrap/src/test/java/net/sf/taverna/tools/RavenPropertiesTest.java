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
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-09-19 12:14:24 $
 *               by   $Author: stain $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RavenPropertiesTest {

	String realTavHome;

	@Before
	public void setUp() throws Exception {
		realTavHome = System.getProperty("taverna.home");
		String resourcePath = RavenPropertiesTest.class.getResource(
				"/conf/raven.properties").toExternalForm();
		resourcePath = resourcePath.replaceAll("file:", "");
		resourcePath = resourcePath.replaceAll("conf/raven.properties", "");
		System.out.println("Looking for conf/raven.properties in: "
				+ resourcePath);
		System.setProperty("taverna.home", resourcePath);
	}

	@After
	public void tearDown() throws Exception {
		if (realTavHome != null)
			System.setProperty("taverna.home", realTavHome);
		else
			System.clearProperty("taverna.home");
		RavenProperties.getInstance().flush();
		System.clearProperty("raven.profilelist");
		System.clearProperty("raven.profile");
	}

	@Test
	public void testRavenProperties() throws Exception {

		System.setProperty("raven.splashscreen", "a splashscreen");

		Properties props = RavenProperties.getInstance().getProperties();

		assertNotNull("No raven.loader.groupid defined", props
				.getProperty("raven.loader.groupid"));
		assertNotNull("No raven.loader.artifactid defined", props
				.getProperty("raven.loader.artifactid"));
		assertNotNull("No raven.loader.version defined", props
				.getProperty("raven.loader.version"));
		assertNotNull("No raven.loader.class defined", props
				.getProperty("raven.loader.class"));
		assertNotNull("No raven.loader.method defined", props
				.getProperty("raven.loader.method"));
		assertNotNull("No raven.repository.11 defined", props
				.getProperty("raven.repository.11"));
		assertNotNull("No raven.splashscreen defined", props
				.getProperty("raven.splashscreen"));
		assertNotNull("No raven.splashscreen.timeout defined", props
				.getProperty("raven.splashscreen.timeout"));
		assertNotNull("No raven.target.groupid defined", props
				.getProperty("raven.target.groupid"));
		assertNotNull("No raven.target.artifactid defined", props
				.getProperty("raven.target.artifactid"));
		assertNotNull("No raven.target.version defined", props
				.getProperty("raven.target.version"));
		assertNotNull("No raven.target.class defined", props
				.getProperty("raven.target.class"));
		assertNotNull("No raven.target.method defined", props
				.getProperty("raven.target.method"));

		// test System overide
		assertEquals("overidden value should be 'a splashscreen'",
				"a splashscreen", props.getProperty("raven.splashscreen"));
	}

	@Ignore("Integration test")
	@Test
	public void testAvailableForUpdatesTrue() {
		System.setProperty("raven.profilelist",
				ProfileSelectorTest.PROFILE_BASE_URL + "test-profilelist.xml");
		assertTrue("updates should be allowed", RavenProperties.getInstance()
				.configuredForUpdates());
	}

	@Test
	public void testAvailableForUpdatesFalse() {
		RavenProperties.getInstance().getProperties().remove(
				"raven.profilelist");
		assertFalse("updates should not be be available", RavenProperties
				.getInstance().configuredForUpdates());
	}

	@Ignore("Integration test")
	@Test
	public void testProfileMirrorList() {
		System.setProperty("raven.profile",
				"http://35E62FF5-324C-4C1B-AB24-4FF6BE7D1C0E.not/profile.xml "
						+ ProfileSelectorTest.PROFILE_BASE_URL
						+ "taverna-1.5.0.0-profile.xml");
		String profile = RavenProperties.getInstance()
				.getRavenProfileLocation();

		assertEquals("List should have been resolved down to 1 that works",
				profile, ProfileSelectorTest.PROFILE_BASE_URL
						+ "taverna-1.5.0.0-profile.xml");
	}

	@Ignore("Integration test")
	@Test
	public void testProfileListMirrorList() {
		System.setProperty("raven.profilelist",
				"http://35E62FF5-324C-4C1B-AB24-4FF6BE7D1C0E.not/profilelist.xml "
						+ ProfileSelectorTest.PROFILE_BASE_URL
						+ "test-profilelist.xml");
		String profileList = RavenProperties.getInstance()
				.getRavenProfileListLocation();

		assertEquals("list should have been resolved down to 1 that works",
				profileList, ProfileSelectorTest.PROFILE_BASE_URL
						+ "test-profilelist.xml");
	}
}
