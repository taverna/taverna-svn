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
package net.sf.taverna.t2.cloudone.datamanager;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.DataPeerImpl;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceSchemeInfo;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link LocationalContext} aspects of the {@link DataManager},
 * {@link DataPeer} and {@link HttpReferenceSchemeInfo}.  
 * 
 * @author Ian Dunlop
 * 
 */
public class HttpRefLocationalContextTest {

	private static final String TEST_NS = "TestNS";

	private static File tmpDir;

	@AfterClass
	public static void deleteTmp() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}

	private FileDataManager fileDataManager;

	private DataPeer dataPeer;

	private Set<LocationalContext> contextSet;

	@Test
	public void checkCurrentNamespace() {
		assertTrue(dataPeer.getCurrentNamespace().equals(
				fileDataManager.getCurrentNamespace()));
	}

	@Test
	public void checkManagedNamespaces() {
		assertTrue(dataPeer.getManagedNamespaces().equals(
				fileDataManager.getManagedNamespaces()));
	}

	@Test
	public void checkURLLocationalContextValid() throws IOException {
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		HttpReferenceScheme urlRef = new HttpReferenceScheme(fileURL);
		assertTrue(urlRef.validInContext(contextSet, dataPeer));
	}

	@Before
	public void createDataManager() {
		Map<String, String> networkName = new HashMap<String, String>();
		networkName.put("type", "NetworkName");
		networkName.put("subnet", "123.456.789.012");
		networkName.put("mask", "098.765.432.109");
		networkName.put("name", "a.b.com");
		Map<String, String> machineName = new HashMap<String, String>();
		machineName.put("type", "MachineName");
		machineName.put("name", "express");
		contextSet = new HashSet<LocationalContext>();
		fileDataManager = new FileDataManager(TEST_NS, contextSet, tmpDir);
		dataPeer = new DataPeerImpl(fileDataManager);
	}

	@Test
	public void URLRefSchemeFactoryTest() {
		Map<String, Set<List<String>>> map = HttpReferenceSchemeInfo
				.getInstance().getRequiredKeys();
		assertTrue(map.containsKey("MachineName"));
	}
}
