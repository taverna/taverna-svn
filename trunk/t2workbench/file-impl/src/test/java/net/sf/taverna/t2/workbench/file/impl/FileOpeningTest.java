package net.sf.taverna.t2.workbench.file.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FileOpeningTest {

	private final ModelMapObserver modelMapObserver = new ModelMapObserver();

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		protected List<ModelMapEvent> messages = new ArrayList<ModelMapEvent>();

		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			messages.add(message);
			if (message.modelName.equals(ModelMapConstants.CURRENT_DATAFLOW)) {
				assertTrue("Dataflow was not listed as open when set current",
						fileManager.getOpenDataflows().contains(
								message.newModel));
			}
		}
	}

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";
	private ModelMap modelmap = ModelMap.getInstance();
	private FileManager fileManager;

	@Before
	public void makeFileManager() {
		fileManager = new FileManagerImpl();
	}

	@Before
	public void listenToModelMap() {
		modelmap.addObserver(modelMapObserver);
	}

	@After
	public void stopListeningToModelMap() {
		modelmap.removeObserver(modelMapObserver);
	}

	@Test
	public void open() throws Exception {
		assertTrue("ModelMapObserver already contained messages",
				modelMapObserver.messages.isEmpty());
		Dataflow dataflow = openDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);
		assertEquals("Loaded dataflow was not set as current dataflow",
				dataflow, modelmap.getModel(ModelMapConstants.CURRENT_DATAFLOW));
		assertFalse("ModelMapObserver did not contain message",
				modelMapObserver.messages.isEmpty());
		assertEquals("ModelMapObserver contained unexpected messages", 1,
				modelMapObserver.messages.size());
		ModelMapEvent event = modelMapObserver.messages.get(0);
		assertEquals("currentDataflow", event.modelName);
		assertEquals(dataflow, event.newModel);
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

	@Test
	public void save() throws Exception {
		Dataflow savedDataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());
		fileManager.saveCurrentDataflow(dataflowFile);
		assertTrue("File should exist", dataflowFile.isFile());
		Dataflow loadedDataflow = fileManager
				.openDataflow(dataflowFile.toURL());
		assertNotSame("Dataflow was not reopened", savedDataflow,
				loadedDataflow);
		assertEquals("Unexpected number of processors in saved dataflow", 1,
				savedDataflow.getProcessors().size());
		assertEquals("Unexpected number of processors in loaded dataflow", 1,
				loadedDataflow.getProcessors().size());

		Processor savedProcessor = savedDataflow.getProcessors().get(0);
		Processor loadedProcessor = loadedDataflow.getProcessors().get(0);
		assertEquals("Loaded processor had wrong name", savedProcessor
				.getLocalName(), loadedProcessor.getLocalName());

		BeanshellActivity savedActivity = (BeanshellActivity) savedProcessor
				.getActivityList().get(0);
		BeanshellActivity loadedActivity = (BeanshellActivity) loadedProcessor
				.getActivityList().get(0);
		String savedScript = savedActivity.getConfiguration().getScript();
		String loadedScript = loadedActivity.getConfiguration().getScript();
		assertEquals("Unexpected saved script",
				"String output = input + \"XXX\";", savedScript);
		assertEquals("Loaded script did not matched saved script", savedScript,
				loadedScript);
	}

	@Test
	public void saveOverwriteWarning() {
		// TODO: Test overwrite warning
	}

	@Test
	public void isChanged() throws Exception {
		Dataflow dataflow = openDataflow();
		assertFalse("Dataflow should not have changed", fileManager
				.isDataflowChanged(dataflow));

		// Do a change
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		Processor emptyProcessor = edits.createProcessor("emptyProcessor");
		Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(dataflow,
				emptyProcessor);
		editManager.doDataflowEdit(dataflow, addProcessorEdit);
		assertTrue("Dataflow should have changed", fileManager
				.isDataflowChanged(dataflow));

		// Save it with the change
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();

		fileManager.saveDataflow(dataflow, dataflowFile);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));
	}
	
	@Ignore("Undo support for ischanged not yet implemented")
	@Test
	public void isChangedWithUndo() throws Exception {
		Dataflow dataflow = openDataflow();
		// Do a change
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		Processor emptyProcessor = edits.createProcessor("emptyProcessor");
		Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(dataflow,
				emptyProcessor);
		editManager.doDataflowEdit(dataflow, addProcessorEdit);
		assertTrue("Dataflow should have changed", fileManager
				.isDataflowChanged(dataflow));
		editManager.undoDataflowEdit(dataflow);
		assertFalse(
				"Dataflow should no longer be marked as changed after undo",
				fileManager.isDataflowChanged(dataflow));
		editManager.redoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after redo before save",
				fileManager.isDataflowChanged(dataflow));

		// Save it with the change
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		fileManager.saveDataflow(dataflow, dataflowFile);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));

		editManager.undoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after undo", fileManager
				.isDataflowChanged(dataflow));
		fileManager.saveDataflow(dataflow, dataflowFile);
		editManager.redoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after redo after save",
				fileManager.isDataflowChanged(dataflow));
	}
	
	@Test
	public void blah() throws Exception {
		Dataflow dataflow = openDataflow();
		
	}

}
