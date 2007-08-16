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
 * Filename           $RCSfile: TestProfileVersions.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-25 11:36:35 $
 *               by   $Author: sowen70 $
 * Created on 16 Jan 2007
 *****************************************************************/
package net.sf.taverna.update.profile;

import java.io.ByteArrayInputStream;
import java.util.List;

import junit.framework.TestCase;

public class TestProfileVersions extends TestCase {

	public void testGettingProfileVersionsFromXML() throws Exception {
		String xml="<profileversions><profile><version>1</version><name>name1</name><location>http://a url1</location></profile><profile><version>2</version><name>name2</name><location>http://a url2</location></profile><profile><version>3</version><name>name3</name><location>http://a url3</location><description>blah blah blah</description></profile></profileversions>";		
		List<ProfileVersion> result = ProfileVersions.getProfileVersions(new ByteArrayInputStream(xml.getBytes()),null);
	
		assertEquals("There should be 3 records",3,result.size());
		assertEquals("The version should be version 1","1",result.get(0).getVersion());
		assertEquals("The name should be version name2","name2",result.get(1).getName());
		assertEquals("The url should be http://a url3","http://a url3",result.get(2).getProfileLocation());
		assertEquals("The description should be 'blah blah blah","blah blah blah",result.get(2).getDescription());
	}
	
	public void testOrderedAscending() throws Exception {
		String xml="<profileversions><profile><version>1.5.2.3</version><name>name1</name><location>http://a url1</location></profile><profile><version>1.5.2.1</version><name>name2</name><location>http://a url2</location></profile><profile><version>1.5.2.2</version><name>name3</name><location>http://a url3</location><description>blah blah blah</description></profile></profileversions>";		
		List<ProfileVersion> result = ProfileVersions.getProfileVersions(new ByteArrayInputStream(xml.getBytes()),null);
		
		assertEquals("1.5.2.1",result.get(0).version);
		assertEquals("1.5.2.2",result.get(1).version);
		assertEquals("1.5.2.3",result.get(2).version);
	}
}
