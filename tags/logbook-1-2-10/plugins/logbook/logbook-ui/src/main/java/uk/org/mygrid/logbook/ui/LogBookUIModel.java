package uk.org.mygrid.logbook.ui;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;

public interface LogBookUIModel {

	public static final String TAVERNA_DATASTORE_CLASS = "taverna.datastore.class";

	public abstract ScuflModel getModel();
	
	public MetadataService getMetadataService();
	
	public DataService getDataService();

	public abstract void refresh() throws MetadataServiceException, DataServiceException;

	public abstract ScuflModel retrieveWorkflow(String workflowLSID)
			throws DataServiceException, NoSuchLSIDException, ScuflException;

	/**
	 * Loads the <code>processRuns</code> into the {@link ScuflModel}.
	 * 
	 * @param processRuns
	 *            ProcessRun[]
	 * @param links
	 *            boolean
	 */
	public abstract void reloadProcesses(ProcessRun[] processRuns,
			boolean links);

	/**
	 * Deletes the workflow run corresponding to <code>workflowRunLSID</code>
	 * from the repository.
	 * 
	 * @param workflowRunLSID
	 *            String
	 */
	public abstract void deleteWorkFlow(String workflowRunLSID);

	/**
	 * Loads the workflow corresponding to <code>workflowLSID</code> into the
	 * {@link ScuflModel}.
	 * 
	 * @param workflowLSID
	 *            String
	 */
	public abstract void reloadWorkFlow(String workflowLSID);

	/**
	 * Reruns <code>workflowRun</code>.
	 * 
	 * @param workflowRun
	 *            WorkflowRun
	 */
	public abstract void rerunWorkflow(WorkflowRun workflowRun);

	public abstract Vector<Workflow> getWorkflowObjects();

	public abstract boolean isWorkflowRun(String workflowLSID);

	// TODO: remove me
	public abstract String getNestedWorkflow(String processURI);

	public abstract List<ProcessRun> getProcessesForWorkflowRun(
			WorkflowRun workflowRun);

	public abstract List<ProcessRun> getProcessesForWorkflowRun(
			String workflowRunLSID);

	public abstract List<ProcessRun> populateIterations(
			List<String> processList, String workflowRunLSID);

	public abstract List<ProcessRun> populateProcessRuns(
			List<String> processList, String workflowRunLSID);

	public abstract Map<String, DataThing> getWorkflowInputs(
			WorkflowRun workflowRun);

	public abstract Map<String, DataThing> getWorkflowInputs(
			String workflowRunLSID);

	/**
	 * 
	 * @param workflowRun
	 * @return
	 */
	public abstract Map<String, DataThing> getWorkflowOutputs(
			WorkflowRun workflowRun);

	public abstract Map<String, DataThing> getProcessInputs(
			ProcessRun processRun);

	public abstract Map<String, DataThing> getProcessOutputs(
			ProcessRun processRun);

	public abstract DataThing fetchDataThing(DataService d, String LSID);

	public abstract String toRDF(String lsid);

}