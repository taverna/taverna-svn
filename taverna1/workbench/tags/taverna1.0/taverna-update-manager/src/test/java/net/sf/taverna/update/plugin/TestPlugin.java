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
 * Filename           $RCSfile: TestPlugin.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-16 13:55:11 $
 *               by   $Author: sowen70 $
 * Created on 15 Jan 2007
 *****************************************************************/
package net.sf.taverna.update.plugin;

import java.io.ByteArrayInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class TestPlugin extends TestCase {

	//test it sets it to 1.5.0 if missing
	public void testMissingTavernaVersion() throws Exception {		
		String xml="<plugin><name>Feta</name><description>Service discovery</description><identifier>uk.org.mygrid.taverna.plugins.feta</identifier><version>1.1.0</version><provider>mygrid.org.uk</provider><repositories><repository>http://www.mygrid.org.uk/maven/repository/</repository></repositories><profile><artifact groupId=\"uk.org.mygrid.feta\" artifactId=\"feta-client\" version=\"1.1.0\"/></profile></plugin>";
		Plugin plugin = createPluginFromXML(xml);
		
		List<String> v=plugin.getTavernaVersions();
		
		assertEquals("should be only 1 version defined",1,v.size());
		assertEquals("should equal 1.5.0","1.5.0",v.get(0));
		
	}
	
	public void testSingleVersion() throws Exception {
		String xml="<plugin><name>Feta</name><description>Service discovery</description><identifier>uk.org.mygrid.taverna.plugins.feta</identifier><version>1.1.0</version><taverna><version>1.5.1</version></taverna><provider>mygrid.org.uk</provider><repositories><repository>http://www.mygrid.org.uk/maven/repository/</repository></repositories><profile><artifact groupId=\"uk.org.mygrid.feta\" artifactId=\"feta-client\" version=\"1.1.0\"/></profile></plugin>";
		Plugin plugin = createPluginFromXML(xml);
		
		List<String> v=plugin.getTavernaVersions();
		
		assertEquals("should be only 1 version defined",1,v.size());
		assertEquals("should equal 1.5.1","1.5.1",v.get(0));
	}
	
	public void testMultipleVersions() throws Exception {
		String xml="<plugin><name>Feta</name><description>Service discovery</description><identifier>uk.org.mygrid.taverna.plugins.feta</identifier><version>1.1.0</version><taverna><version>1.5.0</version><version>1.5.1</version><version>1.5.2</version></taverna><provider>mygrid.org.uk</provider><repositories><repository>http://www.mygrid.org.uk/maven/repository/</repository></repositories><profile><artifact groupId=\"uk.org.mygrid.feta\" artifactId=\"feta-client\" version=\"1.1.0\"/></profile></plugin>";
		Plugin plugin = createPluginFromXML(xml);
		
		List<String> v=plugin.getTavernaVersions();
		
		assertEquals("should be only 3 version defined",3,v.size());
		assertEquals("should equal 1.5.0","1.5.0",v.get(0));
		assertEquals("should equal 1.5.1","1.5.1",v.get(1));
		assertEquals("should equal 1.5.2","1.5.2",v.get(2));
	}			
	
	public void testTavernaCompatibilityInXML() throws Exception{
		String xml="<plugin><name>Feta</name><description>Service discovery</description><identifier>uk.org.mygrid.taverna.plugins.feta</identifier><version>1.1.0</version><taverna><version>1.5.0</version><version>1.5.1</version><version>1.5.2</version></taverna><provider>mygrid.org.uk</provider><repositories><repository>http://www.mygrid.org.uk/maven/repository/</repository></repositories><profile><artifact groupId=\"uk.org.mygrid.feta\" artifactId=\"feta-client\" version=\"1.1.0\"/></profile></plugin>";
		Plugin plugin = createPluginFromXML(xml);
		
		Element element = plugin.toXml();
		
		Element tavernaversions=element.getChild("taverna");
		assertNotNull("xml does not contain taverna version information",tavernaversions);
		
		List<Element> list=tavernaversions.getChildren("version");
		assertEquals("There should be 3 versions defined",3,list.size());
		
	}
	
	private Plugin createPluginFromXML(String xml) throws Exception {
		SAXBuilder builder=new SAXBuilder(false);
		Element el=builder.build(new ByteArrayInputStream(xml.getBytes())).detachRootElement();
		return Plugin.fromXml(el);
	}
	
}
