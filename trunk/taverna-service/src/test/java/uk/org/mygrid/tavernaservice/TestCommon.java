package uk.org.mygrid.tavernaservice;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

public class TestCommon extends TestCase {
	
	public String workflow;
	
	public void setUp() throws IOException {
		loadExampleWorkflow();		
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
