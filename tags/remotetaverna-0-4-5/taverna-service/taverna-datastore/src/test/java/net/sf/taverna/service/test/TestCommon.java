package net.sf.taverna.service.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;

public abstract class TestCommon {

	public static String workflow;

	public static String datadoc;
	

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



}
