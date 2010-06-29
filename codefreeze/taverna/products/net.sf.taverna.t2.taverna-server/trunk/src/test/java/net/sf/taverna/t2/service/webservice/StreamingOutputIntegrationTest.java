/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.webservice;


import java.io.InputStream;

import net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 * @author David Withers
 */
public class StreamingOutputIntegrationTest {

	private TavernaRESTClient tavernaRESTClient;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		tavernaRESTClient = new TavernaRESTClient();
		tavernaRESTClient.setBaseURL("http://localhost:8080/t2server/rest");
		tavernaRESTClient.setCredentials(new UsernamePasswordCredentials("default", "frgsi;y"));
	}

	@Test
	public void testStreamingOutput() throws Exception {
		InputStream is = tavernaRESTClient.testStreaming(5, 1000);
		byte[] bytes = new byte[256];
		int i = is.read(bytes);
		while (i > -1) {
			System.out.print(new String(bytes, 0, i));
			i = is.read(bytes);
		}
		is.close();
	}
	
}
