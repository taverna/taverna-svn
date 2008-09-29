package uk.org.mygrid.logbook.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowMemoryImpl;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;
import uk.org.mygrid.logbook.ui.util.WorkflowRunMemoryImpl;
import uk.org.mygrid.logbook.util.LogBookConstants;
import uk.org.mygrid.logbook.util.WorkflowRunBean;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.dataservice.DataServiceException;

public class LogBookUIMemoryModel extends LogBookUIRemoteModel implements
		LogBookUIModel {

	public static Logger logger = Logger.getLogger(LogBookUIMemoryModel.class);

	public LogBookUIMemoryModel(ScuflModel model, Properties configuration)
			throws MetadataServiceException, DataServiceException {
		super(model, configuration);
	}

	@Override
	public List<ProcessRun> getProcessesForWorkflowRun(WorkflowRun workflowRun) {
		return workflowRun.getProcessRuns();
	}

	@Override
	public Vector<Workflow> getWorkflowObjects() {
		long startTime = System.currentTimeMillis();
		Vector<Workflow> workflowNodes = new Vector<Workflow>();
		Map<String, Set<WorkflowRun>> workflows = new HashMap<String, Set<WorkflowRun>>();
		try {
			Map<String, WorkflowRunBean> workflowRunBeans = getMetadataService()
					.getWorkflowRunBeans();
			List<String> failedWorkflowRuns = getMetadataService()
					.getIndividualsOfType(
							ProvenanceVocab.FAILED_WORKFLOW_RUN.getURI());
			List<String> nestedWorkflowRuns = getMetadataService()
					.getAllNestedRuns();
			for (String workflowRunLSID : getUserWorkflowRuns()) {
				// TODO use workflowRunBeans key set instead of
				// getUserWorkflowRuns()
				WorkflowRunBean workflowRunBean = workflowRunBeans
						.get(workflowRunLSID);
				if (workflowRunBean == null)
					logger.warn("No record for " + workflowRunLSID);
				else {
					WorkflowRun workflowRun = new WorkflowRunMemoryImpl(
							workflowRunLSID, workflowRunBean);
					if (failedWorkflowRuns.contains(workflowRunLSID))
						workflowRun.setFailedWorkflowRun(true);
					if (nestedWorkflowRuns.contains(workflowRunLSID))
						workflowRun.setNestedWorkflowRun(true);
					String workflowInitialId = workflowRun
							.getWorkflowInitialId();
					Set<WorkflowRun> workflowRunsSet = workflows
							.get(workflowInitialId);
					if (workflowRunsSet == null) {
						workflowRunsSet = new HashSet<WorkflowRun>();
						workflows.put(workflowInitialId, workflowRunsSet);
					}
					workflowRunsSet.add(workflowRun);
				}
			}

			/*
			 * Create nodes for the TreeTable with the workflows as parents and
			 * the workflowRuns as children
			 */
			for (Map.Entry<String, Set<WorkflowRun>> entry : workflows
					.entrySet()) {
				Set<WorkflowRun> set = entry.getValue();
				List<WorkflowRun> list = new ArrayList<WorkflowRun>(set);
				Workflow workflow = new WorkflowMemoryImpl(list);
				workflowNodes.add(workflow);
			}
		} catch (LogBookException e) {
			logger.warn(e);
		} catch (ParseException e) {
			logger.warn(e);
		} catch (MetadataServiceException e) {
			logger.warn(e);
		}
		LogBookConstants.logPerformance("getWorkflowObjects", startTime);
		return workflowNodes;
	}

}
