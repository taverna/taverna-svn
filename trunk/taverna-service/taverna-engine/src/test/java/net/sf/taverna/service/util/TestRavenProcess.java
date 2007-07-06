package net.sf.taverna.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

public class TestRavenProcess {

	@Test(expected=ClassNotFoundException.class)
	public void cantFindRaven() throws ClassNotFoundException {
		URLClassLoader cl = new URLClassLoader(new URL[]{}, null);
		cl.loadClass(RavenProcess.RAVEN_CLASS);
	}
	
	@Test
	public void findRaven() throws ClassNotFoundException {
		URL url = RavenProcess.findRaven();
		URLClassLoader cl = new URLClassLoader(new URL[]{url}, null);
		Class<?> p = cl.loadClass(RavenProcess.RAVEN_CLASS);
		assertNotNull(p);
	}

	/**
	 * Does not work properly due to profile problems with taverna.startup
	 * 
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void helloWorld() throws IOException {
		RavenProcess p = new RavenProcess("uk.org.mygrid.tavernaservice", "taverna-engine",
			"1.0.0", "net.sf.taverna.service.util.HelloWorld", "main");
		File mavenRep = new File(new File(System.getProperty("user.home"), ".m2"), "repository");
		assertTrue("Maven repository does not exist", mavenRep.isDirectory());
		p.addSystemProperty("taverna.startup", "/Users/stain/download/taverna-1.5.2");
		
		p.addSystemProperty("raven.repository.0", mavenRep.toString());
		Process proc = p.run();
		assertEquals("Hello world!\n", IOUtils.toString(proc.getInputStream()));
	}
	
	
}
