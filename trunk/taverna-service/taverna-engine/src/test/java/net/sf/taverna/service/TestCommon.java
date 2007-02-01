package net.sf.taverna.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import net.sf.taverna.tools.Bootstrap;

import org.apache.commons.io.IOUtils;

public class TestCommon extends TestCase {
	
	public String workflow;
	private File tavernaHome;
	
	public void setUp() throws IOException {
		loadExampleWorkflow();
		prepareBootstrap();
	}

	void prepareBootstrap() {
		try {
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
		Bootstrap.remoteRepositories = Bootstrap.findRepositories(Bootstrap.properties);
		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
		}
	}

	/**
	 * Load the example workflow
	 * 
	 * @throws IOException
	 */
	void loadExampleWorkflow() throws IOException {
		ClassLoader loader = this.getClass().getClassLoader();
		InputStream stream = loader.getResourceAsStream("uk/org/mygrid/tavernaservice/queue/IterationStrategyExample.xml");		
		workflow = IOUtils.toString(stream);
		assertTrue(workflow.startsWith("<?xml"));
		// (Don't know why there are three \n's.. )
		assertTrue(workflow.endsWith("scufl>\n\n\n"));
	}
	
}
