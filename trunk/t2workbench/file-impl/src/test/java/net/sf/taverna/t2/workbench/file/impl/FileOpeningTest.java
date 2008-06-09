package net.sf.taverna.t2.workbench.file.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FileOpeningTest {

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";
	private ModelMap modelmap = ModelMap.getInstance();
	private FileManager fileManager;

	@Before
	public void makeFileManager() {
		fileManager = new FileManagerImpl();
	}

	@Test
	public void open() throws Exception {
		Dataflow dataflow = openDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);
		assertEquals("Loaded dataflow was not set as current dataflow",
				dataflow, modelmap.getModel(ModelMapConstants.CURRENT_DATAFLOW));
	}

	protected Dataflow openDataflow() throws OpenException {
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		return fileManager.openDataflow(url);
	}

	@Test
	public void isListed() throws Exception {
		assertTrue("Non-empty set of open data flows", fileManager
				.getOpenDataflows().isEmpty());
		Dataflow dataflow = openDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
	}

	@Test
	public void close() throws Exception {
		assertTrue("Non-empty set of open data flows", fileManager
				.getOpenDataflows().isEmpty());
		Dataflow dataflow = openDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
		fileManager.closeDataflow(dataflow, true);
		assertTrue("Non-empty set of open data flows", fileManager
				.getOpenDataflows().isEmpty());
	}
	
	@Ignore("Save not implemented yet")
	@Test
	public void save() throws Exception {
		Dataflow dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());
		fileManager.saveCurrentDataflow(dataflowFile);
		assertTrue("File should exist", dataflowFile.isFile());
		
	}
	

}
