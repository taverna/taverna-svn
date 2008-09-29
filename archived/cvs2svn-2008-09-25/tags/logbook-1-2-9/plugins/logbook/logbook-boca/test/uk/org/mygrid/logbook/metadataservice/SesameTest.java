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
 * Filename           $RCSfile: SesameTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:59:52 $
 *               by   $Author: stain $
 * Created on 18-Aug-2005
 *****************************************************************/
package uk.org.mygrid.logbook.metadataservice;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import uk.org.mygrid.provenance.util.TestUtils;

/**
 * @author dturi
 * @version $Id: NamedRDFGraphsRepositoryTest.java,v 1.1 2005/08/22 10:29:54
 *          turid Exp $
 */
public class SesameTest {

	public static final String RDFS_PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

	static private Properties configuration;

	static private BocaRemoteMetadataService metadataService;

	private String aWorkflowRun;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		configuration = new Properties();
		InputStream inStream = ClassLoader
				.getSystemResourceAsStream("boca.properties");
		configuration.load(inStream);
		inStream.close();
		TestUtils.setTestProperties();
		metadataService = new BocaRemoteMetadataService(configuration);

	}

	@AfterClass
	public static void tearDownClass() {
		if (metadataService != null)
			metadataService.close();
	}
	
	@Test
	public void testToRepository() throws Exception {
		aWorkflowRun = "urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913";
		URL instanceData = ClassLoader.getSystemResource("aWorkflowRun.owl");
		metadataService.storeModel(instanceData, aWorkflowRun);
		//metadataService.toRepository();
		if (metadataService == null)
			return;
		metadataService.removeGraph(aWorkflowRun);
	}
	
	@Test
	public void testToTimeLiteral() throws Exception {
		Literal currentTimeLiteral = JenaSesameHelper.getCurrentTimeLiteral();
		System.out.println(currentTimeLiteral.toString());
		URI datatype = currentTimeLiteral.getDatatype();
		assertEquals("http://www.w3.org/2001/XMLSchema#dateTime", datatype.toString());
	}

	

}
