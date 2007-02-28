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
 * Filename           $RCSfile: TestWSDLReplicator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-28 15:08:45 $
 *               by   $Author: sowen70 $
 * Created on 22 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.wsdl.impl.WSDLReplicatorImpl;

public class TestWSDLReplicator {
	
	private static Logger logger = Logger.getLogger(TestWSDLReplicator.class);

	File tmpDir;
	WSDLReplicator replicator;
	
	@Before
	public void setUp() {
		try {
			tmpDir = File.createTempFile("dataproxy-test", "");
			// But we want a directory!
		} catch (IOException e) {
			logger.error("Couldn't create tmp dir",e);	
		}
		tmpDir.delete();
		tmpDir.mkdir();
		replicator=new WSDLReplicatorImpl("http://localhost:8080/data-proxy/");
	}
	
	@After
	public void deleteTemp() {
		//deleteDir(tmpDir);		
	}
	
	private void deleteDir(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) deleteDir(file);
			else file.delete();
		}
		dir.delete();
	}
	
	@Test
	public void simpleWSDL() throws Exception { 	
		URL wsdlUrl = TestWSDLReplicator.class.getResource("/XEMBL.wsdl");
		String wsdlID="11111";
		replicator.replicateRemoteWSDL(wsdlID,"XEMBL.wsdl",wsdlUrl, tmpDir);
		File wsdlDir = new File(tmpDir,wsdlID);
		
		assertTrue("Directory should exist matching wsdl id",wsdlDir.exists());
		assertTrue("Directory should exist matching wsdl id and be a directory",wsdlDir.isDirectory());
		
		File wsdlCopy = new File(wsdlDir,"XEMBL.wsdl");
		
		assertTrue("WSDL copy doesn't exist",wsdlCopy.exists());		
	}
	
	@Test 
	public void enpointReplacement() throws Exception {
		URL wsdlUrl = TestWSDLReplicator.class.getResource("/XEMBL.wsdl");
		String wsdlID="11111";
		replicator.replicateRemoteWSDL(wsdlID,"XEMBL.wsdl",wsdlUrl, tmpDir);
		File wsdlDir = new File(tmpDir,wsdlID);
		File wsdlCopy = new File(wsdlDir,"XEMBL.wsdl");
		Document doc = new SAXReader().read(new FileInputStream(wsdlCopy));
		
		Element service=doc.getRootElement().element("service");
		Element port=service.element("port");
		Element address=port.element("address");		
		String endpoint=address.attributeValue("location").trim();
		assertTrue("Endpoint should end with /proxy?id=11111",endpoint.endsWith("/proxy?id=11111"));
	}
	
	@Test 
	public void followsExternalSchemas() throws Exception {
		//TODO: use a service on phoebus, under myGrid control
		URL url = new URL("http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/eutils_lite/eutils_lite.wsdl");
		String wsdlID="11111";
		replicator.replicateRemoteWSDL(wsdlID,"XEMBL.wsdl",url, tmpDir);
		
		File wsdlDir = new File(tmpDir,wsdlID);
		File egquery = new File(wsdlDir,"egquery.xsd");
		File einfo = new File(wsdlDir,"einfo.xsd");
		File esearch = new File(wsdlDir,"esearch.xsd");
		File esummary = new File(wsdlDir,"esummary.xsd");
		File elink = new File(wsdlDir,"elink.xsd");
		File espell = new File(wsdlDir,"espell.xsd");
		
		assertTrue("Local copy of schema egquery.xsd does not exist",egquery.exists());
		assertTrue("Local copy of schema einfo.xsd does not exist",einfo.exists());
		assertTrue("Local copy of schema esearch.xsd does not exist",esearch.exists());
		assertTrue("Local copy of schema esummary.xsd does not exist",esummary.exists());
		assertTrue("Local copy of schema elink.xsd does not exist",elink.exists());
		assertTrue("Local copy of schema espell.xsd does not exist",espell.exists());		
	}	
}
