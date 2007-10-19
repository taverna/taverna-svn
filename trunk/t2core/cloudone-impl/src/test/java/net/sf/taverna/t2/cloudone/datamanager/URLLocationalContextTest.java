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

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.impl.url.URLLocationalContext;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceSchemeFactory;
import net.sf.taverna.t2.cloudone.p2p.DataPeerImpl;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link LocationalContext} aspects of the {@link DataManager},
 * {@link DataPeer} and {@link URLReferenceSchemeFactory}.  
 * 
 * @author Ian
 * 
 */
public class URLLocationalContextTest {

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
		URLReferenceScheme urlRef = new URLReferenceScheme(fileURL);
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
		contextSet.add(new URLLocationalContext(machineName));
		contextSet.add(new URLLocationalContext(networkName));
		fileDataManager = new FileDataManager(TEST_NS, contextSet, tmpDir);
		dataPeer = new DataPeerImpl(fileDataManager);
	}

	@Test
	public void URLRefSchemeFactoryTest() {
		Map<String, Set<List<String>>> map = URLReferenceSchemeFactory
				.getInstance().getRequiredKeys();
		assertTrue(map.containsKey("MachineName"));
	}
}
