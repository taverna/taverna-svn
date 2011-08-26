package uk.org.mygrid.logbook.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.WorkflowInputPanelFactory;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.GraphRemovalException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.ProcessRunImpl;
import uk.org.mygrid.logbook.ui.util.ProcessRunWithIterationsImpl;
import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowImpl;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;
import uk.org.mygrid.logbook.ui.util.WorkflowRunImpl;
import uk.org.mygrid.logbook.util.LogBookConstants;
import uk.org.mygrid.logbook.util.Utils;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;
import uk.org.mygrid.provenance.dataservice.DataServiceFactory;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.ProvenanceOntologyUtil;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

public class LogBookUIRemoteModel implements LogBookUIModel {

	public static Logger logger = Logger.getLogger(LogBookUIRemoteModel.class);

	private ScuflModel model;

	private String experimenter;

	private Vector<String> userWorkflowRuns;

	private MetadataService metadataService;

	DataService dataService;

	private Properties configuration;

	public LogBookUIRemoteModel(ScuflModel model, Properties configuration)
			throws MetadataServiceException, DataServiceException {
		this.model = model;
		this.configuration = configuration;
		// getProperties("provenance");

		if (configuration == null)
			logger.warn("provenance properties not found");

		experimenter = configuration.getProperty(
				ProvenanceConfigurator.EXPERIMENTER_KEY,
				ProvenanceConfigurator.DEFAULT_EXPERIMENTER);
		this.refresh();

	}

	public MetadataService getMetadataService() {
		return metadataService;
	}

	public DataService getDataService() {
		return dataService;
	}

	public Vector<String> getUserWorkflowRuns() {
		return userWorkflowRuns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getModel()
	 */
	public ScuflModel getModel() {

		return this.model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#refresh()
	 */
	public void refresh() throws MetadataServiceException, DataServiceException {

		metadataService = MetadataServiceFactory.getInstance(configuration);

		dataService = DataServiceFactory.getInstance(configuration);

		if (metadataService != null) {
			long startTime = System.currentTimeMillis();
			userWorkflowRuns = metadataService.getUserWorkFlows(experimenter);
			LogBookConstants.logPerformance("Workflows loading", startTime);

		}

	}

	/*
	 * Model labelsInstance = rdfRepository.retrieveGraphModel(LabelGraphNS +
	 * experimenter + "test" );
	 * 
	 * if(labelsInstance != null){ System.out.println("Users Labels Graph
	 * found"); usersLabelsOntology = new
	 * JenaProvenanceOntology(labelsInstance); }else{ System.out.println("Users
	 * Labels Graph being created"); usersLabelsOntology = new
	 * JenaProvenanceOntology();
	 * usersLabelsOntology.addLabel(ProvenanceVocab.LABEL.getURI() +
	 * "Interesting");
	 * usersLabelsOntology.addLabel(ProvenanceVocab.LABEL.getURI() + "Draft");
	 * usersLabelsOntology.addLabel(ProvenanceVocab.LABEL.getURI() + "Final");
	 * rdfRepository.storeInstanceData(usersLabelsOntology,LabelGraphNS+experimenter+"test"); }
	 * 
	 * infModelLabels =
	 * ModelFactory.createInfModel(usersLabelsOntology.getOntModel().getReasoner(),usersLabelsOntology.getSchema(),usersLabelsOntology.getModel());
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#retrieveWorkflow(java.lang.String)
	 */
	public ScuflModel retrieveWorkflow(String workflowLSID)
			throws DataServiceException, NoSuchLSIDException, ScuflException {
		dataService.populateWorkflowModel(workflowLSID, model);
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#reloadProcesses(uk.org.mygrid.logbook.ui.util.ProcessRunImpl[],
	 *      boolean)
	 */
	public void reloadProcesses(ProcessRun[] processRuns, boolean links) {
		// TODO create a cache of Models for use by the model rather than load
		// from the DB each time

		long startTime = System.currentTimeMillis();

		try {
			retrieveWorkflow(processRuns[0].getWorkflowLSID());
		} catch (Exception ex) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"There was an error whilst loading the processors",
					"Load Failed", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			logger.error("Error loading the Workflow", ex);
			return;
		}

		Set<Processor> processors = new HashSet<Processor>();
		ScuflModel currentModel = (ScuflModel) ModelMap.getInstance()
				.getNamedModel(ModelMap.CURRENT_WORKFLOW);
		for (int i = 0; i < processRuns.length; i++) {
			try {
				Processor p = model.locateProcessor(processRuns[i].getName());
				currentModel.addProcessor(p);
				processors.add(p);
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
		if (links) {
			DataConstraint[] dc = model.getDataConstraints();
			for (int j = 0; j < dc.length; j++) {
				Processor source = dc[j].getSource().getProcessor();
				Processor sink = dc[j].getSink().getProcessor();
				if (processors.contains(source) && processors.contains(sink)) {
					currentModel.addDataConstraint(dc[j]);
				}
			}
		}

		LogBookConstants.logPerformance("Processors reloading", startTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#deleteWorkFlow(java.lang.String)
	 */
	public void deleteWorkFlow(String workflowRunLSID) {
		try {
			logger.debug("Removing graph = " + workflowRunLSID);
			metadataService.removeWorkflowRun(workflowRunLSID);
		} catch (GraphRemovalException e) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"There was an error whilst deleting the workflow run "
							+ workflowRunLSID, "Deletion Failed",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,
					options, options[0]);
			logger.error("Error deleting " + workflowRunLSID, e);
			return;
		} catch (MetadataServiceException e) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"There was an error whilst deleting the workflow run "
							+ workflowRunLSID, "Deletion Failed",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,
					options, options[0]);
			logger.error("Error deleting " + workflowRunLSID, e);
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#reloadWorkFlow(java.lang.String)
	 */
	public void reloadWorkFlow(String workflowLSID) {
		try {
			ScuflModel clonedModel = model.clone();
			Workbench.getInstance().getWorkflowModels().addModel(clonedModel);
		} catch (Exception ex) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"There was an error whilst loading the processors",
					"Load Failed", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			logger.error("Error loading the Workflow", ex);
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#rerunWorkflow(uk.org.mygrid.logbook.ui.util.WorkflowRun)
	 */
	public void rerunWorkflow(WorkflowRun workflowRun) {
		try {
			ScuflModel clonedModel = model.clone();
			Workbench.getInstance().getWorkflowModels().addModel(clonedModel);
			if (clonedModel.getWorkflowSourcePorts().length != 0) {
				Map<String, DataThing> workflowInputs = getWorkflowInputs(workflowRun);
				WorkflowInputPanelFactory.invokeWorkflow(clonedModel,
						workflowInputs);
			} else {
				WorkflowInputPanelFactory.invokeWorkflow(clonedModel);
			}

		} catch (Exception ex) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"There was an error whilst loading the processors",
					"Load Failed", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			logger.error("Error loading the Workflow", ex);
			return;
		}
	}

	// public ArrayList getLabelObjects() {

	// Map labelsMap = new HashMap();
	// List labelsList = usersLabelsOntology.getLabelsList();
	// ArrayList labelNodes = new ArrayList();
	//
	// for (int i = 0; i < labelsList.size(); i++) {
	//
	// Label label = new Label((String) labelsList.get(i));
	//
	// NodeIterator ni = infModelLabels.listObjectsOfProperty(infModel
	// .getResource(labelsList.get(i).toString()),
	// ProvenanceVocab.LABELS);
	// ArrayList workflowRuns = new ArrayList();
	//
	// while (ni.hasNext()) {
	// WorkflowRun wR = new WorkflowRun();
	// String runLSID = (String) ni.next();
	// wR.setLSID(runLSID);
	// wR.setAuthor("");
	// try {
	// String Date = usersWorkFlowOntology
	// .getUnparsedProcessEndDate(runLSID);
	// Date date = JenaProvenanceOntology.parseDateTime(Date);
	//
	// wR.setDate(date);
	// } catch (Exception e) {
	// }
	// wR
	// .setDescription(usersWorkFlowOntology.getPropertyValue(
	// runLSID, ProvenanceVocab.WORKFLOW_DESCRIPTION
	// .getURI()));
	// wR.setTitle(usersWorkFlowOntology.getPropertyValue(runLSID,
	// ProvenanceVocab.WORKFLOW_TITLE.getURI()));
	//
	// workflowRuns.add(wR);
	//
	// }
	//
	// label.setWorkflowRuns(workflowRuns.toArray());
	// labelNodes.add(label);
	// }
	//
	// return labelNodes;
	//
	// }

	// public List getUsersLabels() {
	//  
	// return new ArrayList();// usersLabelsOntology.getLabelsList();
	//  
	// }

	// public void addLabel(String Label) {
	//  
	// usersLabelsOntology.addLabel(ProvenanceVocab.LABEL.getURI() + Label);
	//
	// }

	// public void addHasLabel(String Label) {
	//  
	// usersWorkFlowOntology.addHasLabel(this.experimenter,
	// ProvenanceVocab.LABEL.getURI() + Label);
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getWorkflowObjects()
	 */
	public Vector<Workflow> getWorkflowObjects() {
		long startTime = System.currentTimeMillis();
		Vector<Workflow> workflowNodes = new Vector<Workflow>();
		Map<String, Set<String>> workflows = new HashMap<String, Set<String>>();
		Set<String> dataCollection;
		try {
			for (int i = 0; i < userWorkflowRuns.size(); i++) {
				String workflowRunLSID = (String) userWorkflowRuns.get(i);
				String workflowID = metadataService
						.getFirstObjectPropertyValue(workflowRunLSID,
								ProvenanceVocab.RUNS_WORKFLOW.getURI());
				String workflowLSID = metadataService
						.getFirstDatatypePropertyValue(workflowID,
								ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI());

				if (workflows.containsKey(workflowLSID))
					dataCollection = workflows.get(workflowLSID);
				else {
					dataCollection = new HashSet<String>();
					workflows.put(workflowLSID, dataCollection);
				}
				dataCollection.add(workflowRunLSID);
			}

			/*
			 * Create nodes for the TreeTable with the workflows as parents and
			 * the workflowRuns as children
			 */
			for (String lsid : workflows.keySet()) {
				Workflow workflow = new WorkflowImpl();

				Set<String> workflowRuns = workflows.get(lsid);
				Vector<WorkflowRun> children = new Vector<WorkflowRun>();
				// process the workflowRuns for the workflow
				for (String runLSID : workflowRuns) {
					WorkflowRun workflowRun = new WorkflowRunImpl();
					workflowRun.setLsid(runLSID);
					workflowRun.setWorkflowInitialId(metadataService
							.getFirstObjectPropertyValue(runLSID,
									ProvenanceVocab.RUNS_WORKFLOW.getURI()));
					workflowRun.setIcon(TavernaIcons.runIcon);
					String dateString;
					Date date = new Date();

					dateString = metadataService
							.getUnparsedWorkflowStartDate(workflowRun.getLsid());
					try {
						date = parseDate(dateString);
					} catch (ParseException e) {
						logger.error(e);
					}

					workflowRun.setDate(date);

					workflowRun.setTitle(metadataService
							.getFirstDatatypePropertyValue(runLSID,
									ProvenanceVocab.WORKFLOW_TITLE.getURI()));

					workflowRun
							.setNestedWorkflowRun(metadataService
									.isIndividualOfType(
											runLSID,
											ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWRUN));
					workflowRun
							.setFailedWorkflowRun(metadataService
									.isIndividualOfType(
											runLSID,
											ProvenanceOntologyConstants.Classes.FAILEDWORKFLOWRUN));

					if (workflowRun.isFailedWorkflowRun())
						workflowRun.setIcon(TavernaIcons.deleteIcon);

					children.add(workflowRun);

				}

				WorkflowRun[] runs = new WorkflowRun[children.size()];
				for (int i = 0; i < runs.length; i++) {
					runs[i] = children.get(i);
				}
				workflow.setWorkflowRuns(runs);
				// each time a worklfow is run the workflow SCUFL is stored as a
				// snapshot and assigned a new LSID
				// to get round the fact that Taverna doesn't update a workflows
				// LSID even if the data(workflow)has changed
				// we use the latest run to find the latest stored workflow
				// SCUFL to
				// populate the workflow details
				WorkflowRun latestRun = workflow.getLatestRun();
				String latestWorkflowLSID = latestRun.getWorkflowInitialId();

				workflow.setLsid(latestWorkflowLSID);

				String Author = metadataService.getFirstDatatypePropertyValue(
						latestWorkflowLSID, ProvenanceVocab.WORKFLOW_AUTHOR
								.getURI());

				workflow.setAuthor(Author);
				workflow.setDate(null);

				String Description = metadataService
						.getFirstDatatypePropertyValue(latestWorkflowLSID,
								ProvenanceVocab.WORKFLOW_DESCRIPTION.getURI());
				workflow.setDescription(Description);

				String Title = metadataService.getFirstDatatypePropertyValue(
						latestWorkflowLSID, ProvenanceVocab.WORKFLOW_TITLE
								.getURI());

				workflow.setTitle(Title);

				workflowNodes.add(workflow);
			}

			Workflow[] workflowsArray = new Workflow[workflowNodes.size()];
			workflowNodes.toArray(workflowsArray);
			Arrays.sort(workflowsArray);
			workflowNodes = new Vector<Workflow>(Arrays.asList(workflowsArray));
		} catch (MetadataServiceException e) {
			logger.error(e);
		} catch (LogBookException e) {
			logger.warn(e);
		}
		LogBookConstants.logPerformance("getWorkflowObjects", startTime);
		return workflowNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#isWorkflowRun(java.lang.String)
	 */
	public boolean isWorkflowRun(String workflowLSID) {
		try {
			return metadataService.getFirstObjectPropertyValue(workflowLSID,
					ProvenanceVocab.RUNS_WORKFLOW.getURI()) != null;
		} catch (MetadataServiceException e) {
			logger.error(e);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getNestedWorkflow(java.lang.String)
	 */
	public String getNestedWorkflow(String processURI) {

		try {
			return metadataService.getFirstObjectPropertyValue(processURI,
					ProvenanceVocab.NESTED_WORKFLOW.getURI());
		} catch (MetadataServiceException e) {
			logger.error(e);
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getProcessesForWorkflowRun(uk.org.mygrid.logbook.ui.util.WorkflowRun)
	 */
	public List<ProcessRun> getProcessesForWorkflowRun(WorkflowRun workflowRun) {
		return getProcessesForWorkflowRun(workflowRun.getLsid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getProcessesForWorkflowRun(java.lang.String)
	 */
	public List<ProcessRun> getProcessesForWorkflowRun(String workflowRunLSID) {
		long startTime = System.currentTimeMillis();
		List<String> processList = new ArrayList<String>();
		try {
			processList = metadataService.getProcessesRuns(workflowRunLSID);
		} catch (MetadataServiceException e) {
			logger.error("Error retrieving instances from the Ontology", e);
		}
		List<ProcessRun> result = populateProcessRuns(processList,
				workflowRunLSID);
		LogBookConstants
				.logPerformance("getProcessesForWorkflowRun", startTime);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#populateProcessRuns(java.util.List,
	 *      java.lang.String)
	 */
	public List<ProcessRun> populateProcessRuns(List<String> processList,
			String workflowRunLSID) {

		Iterator i = processList.iterator();
		List<ProcessRun> processRuns = new ArrayList<ProcessRun>();

		try {
			while (i.hasNext()) {

				ProcessRunImpl p = new ProcessRunImpl();
				Object o = i.next();
				if (o != null) {
					String processLSID = o.toString();
					if (!metadataService.isProcessIteration(processLSID)) {

						if (metadataService
								.isProcessWithIterations(processLSID)) {
							p = new ProcessRunWithIterationsImpl();
							List<String> iterationsLSIDList = new ArrayList<String>();
							iterationsLSIDList = metadataService
									.getObjectPropertyValues(
											processLSID,
											ProvenanceOntologyConstants.ObjectProperties.ITERATION);

							logger.info(iterationsLSIDList);
							iterationsLSIDList.remove(processLSID);
							List<ProcessRun> iterationsList = populateIterations(
									iterationsLSIDList, workflowRunLSID);
							((ProcessRunWithIterationsImpl) p)
									.setIterations(iterationsList);
						}

						String dateString;
						Date date = new Date();
						try {

							dateString = metadataService
									.getUnparsedProcessEndDate(processLSID);

							date = parseDate(dateString);
							p.setDate(date);

						} catch (Exception pe) {
							logger.error("Parse exception", pe);
						}

						String name = metadataService
								.getFirstObjectPropertyValue(processLSID,
										ProvenanceVocab.RUNS_PROCESS.getURI());
						p.setLsid(processLSID);
						if (name != null) {
							String[] nameSplit = name.split("#");
							if (nameSplit.length > 1) {
								p.setName(nameSplit[1]);
							} else {
								p.setName(name);
							}
						} else {
							logger.warn("Name for process " + processLSID
									+ " is null.");
						}
						p
								.setWorkflowLSID(metadataService
										.getFirstObjectPropertyValue(
												workflowRunLSID,
												ProvenanceVocab.RUNS_WORKFLOW
														.getURI()));
						p.setWorkflowRunLSID(workflowRunLSID);

						if (metadataService
								.isIndividualOfType(
										processLSID,
										ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)) {

							p.setFailed(true);
							String causeString = metadataService
									.getFirstDatatypePropertyValue(processLSID,
											ProvenanceVocab.CAUSE.getURI());
							p.setCause(causeString);

						} else {
							p.setFailed(false);

						}

						if (metadataService
								.isIndividualOfType(
										processLSID,
										ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWPROCESSRUN)) {
							p.setSubWorkflow(true);
						} else {
							p.setSubWorkflow(false);
						}
						String process = metadataService
								.getFirstObjectPropertyValue(processLSID,
										ProvenanceVocab.RUNS_PROCESS.getURI());
						String processClassName = metadataService
								.getFirstDatatypePropertyValue(process,
										ProvenanceVocab.CLASS_NAME.getURI());

						String tagName = ProcessorHelper
								.getTagNameForClassName(processClassName);

						ImageIcon icon = ProcessorHelper
								.getIconForTagName(tagName);

						p.setIcon(icon);

						processRuns.add(p);

					}
				}
			}
		} catch (MetadataServiceException e) {
			logger.error(e);
		}
		ProcessRun[] processRunsArray = new ProcessRun[processRuns.size()];
		processRuns.toArray(processRunsArray);
		Arrays.sort(processRunsArray);
		processRuns = Arrays.asList(processRunsArray);
		return processRuns;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#populateIterations(java.util.List,
	 *      java.lang.String)
	 */
	public List<ProcessRun> populateIterations(List<String> processList,
			String workflowRunLSID) {
		long startTime = System.currentTimeMillis();
		Iterator i = processList.iterator();
		List<ProcessRun> processRuns = new ArrayList<ProcessRun>();
		try {
			while (i.hasNext()) {

				ProcessRun p = new ProcessRunImpl();
				Object o = i.next();

				if (o != null) {
					String processLSID = o.toString();

					if (metadataService.isProcessWithIterations(processLSID)) {
						p = new ProcessRunWithIterationsImpl();
						List<String> iterationsLSIDList = new ArrayList<String>();
						iterationsLSIDList = metadataService
								.getObjectPropertyValues(
										processLSID,
										ProvenanceOntologyConstants.ObjectProperties.ITERATION);
						logger.info(iterationsLSIDList);
						// if a processWithIterations holds itself as an
						// iteration
						// remove for obvious reasons!
						iterationsLSIDList.remove(processLSID);

						List<ProcessRun> iterationsList = new ArrayList<ProcessRun>();

						iterationsList = populateIterations(iterationsLSIDList,
								workflowRunLSID);

						((ProcessRunWithIterationsImpl) p)
								.setIterations(iterationsList);

					}

					String name = metadataService.getFirstObjectPropertyValue(
							processLSID, ProvenanceVocab.RUNS_PROCESS.getURI());
					String dateString;
					Date date = new Date();

					dateString = metadataService
							.getUnparsedProcessEndDate(processLSID);
					try {
						date = parseDate(dateString);
					} catch (ParseException e) {
						logger.error(e);
					}
					p.setDate(date);

					p.setLsid(processLSID);
					String[] nameSplit = name.split("#");
					if (nameSplit.length > 1) {
						p.setName(nameSplit[1]);
					} else {
						p.setName(name);
					}

					p.setWorkflowLSID(metadataService
							.getFirstObjectPropertyValue(workflowRunLSID,
									ProvenanceVocab.RUNS_WORKFLOW.getURI()));
					p.setWorkflowRunLSID(workflowRunLSID);

					if (metadataService
							.isIndividualOfType(
									processLSID,
									ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)) {

						p.setFailed(true);
						String causeString = metadataService
								.getFirstDatatypePropertyValue(processLSID,
										ProvenanceVocab.CAUSE.getURI());
						p.setCause(causeString);

					} else {
						p.setFailed(false);

					}

					if (metadataService
							.isIndividualOfType(
									processLSID,
									ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWPROCESSRUN)) {
						p.setSubWorkflow(true);
					} else {
						p.setSubWorkflow(false);
					}
					String process = metadataService
							.getFirstObjectPropertyValue(processLSID,
									ProvenanceVocab.RUNS_PROCESS.getURI());
					String processClassName = metadataService
							.getFirstDatatypePropertyValue(process,
									ProvenanceVocab.CLASS_NAME.getURI());

					String tagName = ProcessorHelper
							.getTagNameForClassName(processClassName);

					ImageIcon icon = ProcessorHelper.getIconForTagName(tagName);

					p.setIcon(icon);

					processRuns.add(p);

				}
			}

			ProcessRun[] processRunsArray = new ProcessRun[processRuns.size()];
			processRuns.toArray(processRunsArray);
			Arrays.sort(processRunsArray);
			processRuns = new ArrayList<ProcessRun>(Arrays
					.asList(processRunsArray));
		} catch (MetadataServiceException e) {
			logger.error(e);
		}
		LogBookConstants.logPerformance("populateIterations", startTime);
		return processRuns;

	}

	public static Date parseDate(String dateString) throws ParseException {
		Date parsedDate = new Date();
		parsedDate = ProvenanceOntologyUtil.parseDateTime(dateString);
		return parsedDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getWorkflowInputs(uk.org.mygrid.logbook.ui.util.WorkflowRun)
	 */
	public Map<String, DataThing> getWorkflowInputs(WorkflowRun workflowRun) {
		String workflowRunLSID = workflowRun.getLsid();
		return getWorkflowInputs(workflowRunLSID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getWorkflowInputs(java.lang.String)
	 */
	public Map<String, DataThing> getWorkflowInputs(String workflowRunLSID) {
		Map<String, DataThing> result = new HashMap<String, DataThing>();

		List inputList = new ArrayList();

		try {
			inputList = metadataService.getWorkflowInputs(workflowRunLSID);
		} catch (MetadataServiceException e) {
			logger.error("Problem retrieving Instances from the ontology", e);
		}

		// Iterator i = outputList.iterator();
		// NodeIterator n = infModel.listObjectsOfProperty(infModel
		// .getResource(workflowRun.getLSID()),
		// ProvenanceVocab.WORKFLOW_OUTPUT);

		for (int i = 0; i < inputList.size(); i++) {
			DataThing d = null;

			String lsid = (String) inputList.get(i);
			// DataThing d = fetchDataThing(jdbcb, lsid);
			try {
				d = fetchDataThing(dataService, lsid);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			try {
				List<String> dataNames = metadataService
						.getObjectPropertyValues(
								lsid,
								ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME);
				String dataName = null;
				Iterator<String> iterator = dataNames.iterator();
				while (iterator.hasNext() && dataName == null) {
					String name = iterator.next();
					if (name.startsWith(ProvenanceGenerator.WORKFLOW_NS))
						dataName = name;
				}
				dataName = Utils.inputLocalName(dataName);
				result.put(dataName, d);
			} catch (MetadataServiceException e) {
				logger.error(e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getWorkflowOutputs(uk.org.mygrid.logbook.ui.util.WorkflowRun)
	 */
	public Map<String, DataThing> getWorkflowOutputs(WorkflowRun workflowRun) {

		Map<String, DataThing> result = new HashMap<String, DataThing>();

		List outputList = new ArrayList();

		try {
			outputList = metadataService.getWorkflowOutputs(workflowRun
					.getLsid());
		} catch (MetadataServiceException e) {
			logger.error("Problem retrieving Instances from the ontology", e);
		}

		// Iterator i = outputList.iterator();
		// NodeIterator n = infModel.listObjectsOfProperty(infModel
		// .getResource(workflowRun.getLSID()),
		// ProvenanceVocab.WORKFLOW_OUTPUT);

		for (int i = 0; i < outputList.size(); i++) {
			DataThing d = null;

			String lsid = (String) outputList.get(i);
			// DataThing d = fetchDataThing(jdbcb, lsid);
			try {
				d = fetchDataThing(dataService, lsid);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			try {
				List<String> dataNames = metadataService
						.getObjectPropertyValues(
								lsid,
								ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME);
				for (String dataName : dataNames) {
					if (dataName.startsWith(ProvenanceGenerator.WORKFLOW_NS)) {
						result.put(dataName, d);
					}
				}
			} catch (MetadataServiceException e) {
				logger.error(e);
			}
		}

		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getProcessInputs(uk.org.mygrid.logbook.ui.util.ProcessRunImpl)
	 */
	public Map<String, DataThing> getProcessInputs(ProcessRun processRun) {
		return getProcessIntermediates(processRun,
				ProvenanceVocab.PROCESS_INPUT);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#getProcessOutputs(uk.org.mygrid.logbook.ui.util.ProcessRunImpl)
	 */
	public Map<String, DataThing> getProcessOutputs(ProcessRun processRun) {

		return getProcessIntermediates(processRun,
				ProvenanceVocab.PROCESS_OUTPUT);

	}

	private Map<String, DataThing> getProcessIntermediates(
			ProcessRun processRun, ObjectProperty p) {
		Map<String, DataThing> result = new HashMap<String, DataThing>();

		Set<String> intermediatesList = null;

		try {
			if (p == ProvenanceVocab.PROCESS_INPUT)
				intermediatesList = new HashSet<String>(metadataService
						.getProcessInputs(processRun.getLsid()));
			else if (p == ProvenanceVocab.PROCESS_OUTPUT)
				intermediatesList = new HashSet<String>(metadataService
						.getProcessOutputs(processRun.getLsid()));
		} catch (MetadataServiceException e) {
			logger.error("Problem retrieving instances from the ontology", e);
		}

		// Iterator i = outputList.iterator();
		// NodeIterator n = infModel.listObjectsOfProperty(infModel
		// .getResource(workflowRun.getLSID()),
		// ProvenanceVocab.WORKFLOW_OUTPUT);

		for (String lsid : intermediatesList) {
			DataThing d = null;
			// DataThing d = fetchDataThing(jdbcb, lsid);
			try {
				d = fetchDataThing(dataService, lsid);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			try {
				Set<String> dataNames = null;
				if (p == ProvenanceVocab.PROCESS_INPUT)
					dataNames = new HashSet<String>(
							metadataService
									.getObjectPropertyValues(
											lsid,
											ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME));
				else if (p == ProvenanceVocab.PROCESS_OUTPUT)
					dataNames = new HashSet<String>(
							metadataService
									.getObjectPropertyValues(
											lsid,
											ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME));
				if (dataNames != null)
					for (String dataName : dataNames) {
						if (dataName.contains(processRun.getName())) {
							result.put(dataName, d);
						}
					}
			} catch (MetadataServiceException e) {
				logger.error(e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#fetchDataThing(uk.org.mygrid.provenance.dataservice.DataService,
	 *      java.lang.String)
	 */
	public DataThing fetchDataThing(DataService d, String LSID) {

		try {

			if (d == null)
				return new DataThing(
						"Cannot retrieve data - Data Store not specified");
			return d.fetchDataThing(LSID);
		} catch (NoSuchLSIDException nslside) {
			return new DataThing(
					"Problem Retrieving Data from Store - No Data Matching the Given LSID Value");
		} catch (DataServiceException dse) {
			return new DataThing("Problem with the Data Store");
		}

	}

	// a method used to debug and print out the details of an Resource in the
	// Ontology.
	public void printStatements(Model m, Resource s, Property p, Resource o) {
		for (StmtIterator i = m.listStatements(s, p, o); i.hasNext();) {
			Statement stmt = i.nextStatement();
			System.out.println(" - " + PrintUtil.print(stmt));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.LogBookUIModelInterface#toRDF(java.lang.String)
	 */
	public String toRDF(String lsid) {
		String graph = null;
		try {
			graph = metadataService.retrieveGraph(lsid);
		} catch (MetadataServiceException e) {
			logger.error(e);
		}
		return graph;
	}

}
