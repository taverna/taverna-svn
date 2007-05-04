package net.sf.taverna.service.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import net.sf.taverna.service.backend.Engine;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.tools.Bootstrap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

public abstract class TestCommon {

	private static File tavernaHome;

	public static String workflow;

	public static String datadoc;
	
	/**
	 * Create a temporary taverna.home, fresh and empty.
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws MalformedURLException 
	 */
	@BeforeClass
	public synchronized static void prepareBootstrap() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		if (Bootstrap.properties == null) {
			Engine.bootstrap();
			Engine.init();
		}

		
/*		try {
			tavernaHome = File.createTempFile("taverna", "home");
			tavernaHome.delete();
			tavernaHome.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("Can't make taverna.home");
		}
		System.setProperty("taverna.home", tavernaHome.getAbsolutePath());
		Bootstrap.findUserDir();
		Bootstrap.properties = Bootstrap.findProperties();
		Bootstrap.remoteRepositories =
			Bootstrap.findRepositories(Bootstrap.properties);
		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
		}
		File logDir = new File(tavernaHome, "logs");
		System.setProperty("taverna.logdir", logDir.getAbsolutePath());*/
	}

	@BeforeClass
	/**
	 * Load the example workflow
	 * 
	 * @throws IOException
	 */
	public static void loadExampleWorkflow() throws IOException {
		ClassLoader loader = TestCommon.class.getClassLoader();
		InputStream stream =
			loader.getResourceAsStream("net/sf/taverna/service/test/IterationStrategyExample.xml");
		workflow = IOUtils.toString(stream);
		assertTrue(workflow.startsWith("<?xml"));
		// (Don't know why there are three \n's.. )
		assertTrue(workflow.endsWith("scufl>\n\n\n"));
	}
	
	@BeforeClass
	public static void loadExampleDataDoc() throws IOException {
		ClassLoader loader = TestCommon.class.getClassLoader();
		InputStream stream =
			loader.getResourceAsStream("net/sf/taverna/service/test/remove_duplicates_input.xml");
		datadoc = IOUtils.toString(stream);
		assertTrue(datadoc.startsWith("<?xml"));
		// (Don't know why there are three \n's.. )
		assertTrue(datadoc.endsWith("dataThingMap>\n\n"));
	}
	
	/**
	 * Remove that temporary taverna.home
	 * 
	 * @throws IOException
	 */
	@AfterClass
	public static void deleteTavernaHome() throws IOException {
		try {
			if (tavernaHome != null) {
				FileUtils.deleteDirectory(tavernaHome);
			}
		} finally {
			tavernaHome = null;
			System.clearProperty("taverna.home");
		}
	}


}
