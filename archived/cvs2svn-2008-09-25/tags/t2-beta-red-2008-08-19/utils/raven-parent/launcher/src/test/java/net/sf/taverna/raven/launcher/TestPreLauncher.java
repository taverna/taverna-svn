package net.sf.taverna.raven.launcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import net.sf.taverna.raven.appconfig.AbstractPropThreadTest;
import net.sf.taverna.raven.prelauncher.PreLauncher;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TestPreLauncher extends AbstractPropThreadTest {

	@Test
	public void launchHelloWorld() throws Exception {
		System.setProperty("raven.launcher.app.name", "helloworld");
		System.setProperty("raven.launcher.app.main",
				"net.sf.taverna.raven.helloworld.HelloWorld");
		List<URL> urls = makeClassPath("TestPreLauncher-helloworld/");
		URLClassLoader classLoader = new URLClassLoader(urls
				.toArray(new URL[0]), getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
		classLoader.loadClass("org.apache.log4j.Logger");

		File outFile = File.createTempFile(getClass().getCanonicalName(),
				"test");
		outFile.deleteOnExit();

		PreLauncher.main(new String[] { outFile.getAbsolutePath() });
		String out = FileUtils.readFileToString(outFile, "utf8");
		assertEquals("Did not match expected output",
				"This is the test data.\n", out);
	}
}
