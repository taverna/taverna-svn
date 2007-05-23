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
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-23 16:17:41 $
 *               by   $Author: sowen70 $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.tools;

import java.util.Properties;

import junit.framework.TestCase;

public class RavenPropertiesTest extends TestCase{	
	
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
	}



	public void testRavenProperties() throws Exception{
		
		System.setProperty("raven.splashscreen","a splashscreen");
		
		Properties props = new RavenProperties().getProperties();		
		
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
		assertNotNull("No raven.remoteprofile defined",props.getProperty("raven.remoteprofile"));
		
		//test System overide
		assertEquals("overidden value should be 'a splashscreen'","a splashscreen",props.getProperty("raven.splashscreen"));		
	}
}
