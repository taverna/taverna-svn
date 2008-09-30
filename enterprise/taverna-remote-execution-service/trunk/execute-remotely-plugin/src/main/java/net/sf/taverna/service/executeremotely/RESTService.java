package net.sf.taverna.service.executeremotely;

import java.util.Map;

import net.sf.taverna.service.rest.client.DataREST;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.WorkflowREST;
import net.sf.taverna.service.rest.client.WorkflowsREST;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.restlet.util.Engine;

import com.noelios.restlet.ext.httpclient.HttpClientHelper;

/**
 * Wrapper class for {@link RESTContext} that provides common operations for the
 * Taverna plugin, such as submitting a workflow job.
 * 
 * @author Stian Soiland
 * 
 */
public class RESTService {

	private static Logger logger = Logger.getLogger(RESTService.class);

	static {
		ClassLoader cl = RESTContext.class.getClassLoader();
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		Engine.setClassLoader(cl);
		com.noelios.restlet.Engine e = new com.noelios.restlet.Engine(false);
		Engine.setInstance(e);
		e.getRegisteredClients().add(new HttpClientHelper(null));
	}

	private static XMLOutputter xmlOutputter = new XMLOutputter(Format
			.getCompactFormat());

	private final RESTContext context;

	/**
	 * Construct with a given {@link RESTContext}. All connection info is
	 * provided by the context.
	 * 
	 * @param context
	 *            The connection parameters to use
	 */
	public RESTService(RESTContext context) {
		this.context = context;
	}

	/**
	 * Upload a workflow to the service. The returned {@link WorkflowREST} can
	 * be passed to {@link #addJob(WorkflowREST)} and
	 * {@link #addJob(WorkflowREST, DataREST)}
	 * 
	 * @param model
	 *            The ScuflModel to upload
	 * @return The {@link WorkflowREST} instance referencing the uploaded
	 *         workflow
	 * @throws NotSuccessException
	 *             If the workflow could not be uploaded
	 */
	public WorkflowREST uploadWorkflow(ScuflModel model)
			throws NotSuccessException {
		String scufl = XScuflView.getXMLText(model);
		WorkflowsREST userWfs = context.getUser().getWorkflows();
		WorkflowREST wf = userWfs.add(scufl);
		logger.info("Created new workflow " + wf);
		return wf;
	}

	/**
	 * Return the context, which is the URI, username and password of the
	 * service.
	 * 
	 * @return
	 */
	public RESTContext getContext() {
		return context;
	}

	/**
	 * Upload a map of data for a job to the service. The returned
	 * {@link DataREST} can be passed to {@link #addJob(WorkflowREST, DataREST)}.
	 * 
	 * @param inputs
	 *            The {@link Map} of {@link String}s to {@link DataThing}s
	 *            representing the workflow input port values.
	 * @return The {@link DataREST} instance referencing the uploaded data
	 *         document.
	 * @throws NotSuccessException
	 *             If the data could not be uploaded
	 */
	public DataREST uploadData(Map<String, DataThing> inputs)
			throws NotSuccessException {
		// From XMLUtils.makeDataDocument()
		org.jdom.Document doc = DataThingXMLFactory.getDataDocument(inputs);

		// Upload data
		String baclava = xmlOutputter.outputString(doc);
		return context.getUser().getDatas().add(baclava);
	}

	/**
	 * Add a job to the service. A job is a particular run of a workflow with
	 * given inputs. The returned {@link JobREST} can be checked for its status
	 * using {@link JobREST#getStatus()} and eventually it's outputs can be
	 * fetched using {@link JobREST#getOutputs()}.
	 * 
	 * @param wf
	 *            The {@link WorkflowREST} instance created using
	 *            {@link #uploadWorkflow(ScuflModel)
	 * @param inputDoc
	 *            The {@link DataREST} instance created using
	 *            {@link #uploadData(Map)}
	 * @return The {@link JobREST} instance referencing the newly created job
	 * @throws NotSuccessException
	 *             If the job could not be created
	 */
	public JobREST addJob(WorkflowREST wf, DataREST inputDoc)
			throws NotSuccessException {
		return context.getUser().getJobs().add(wf, inputDoc);
	}

	/**
	 * Add a job to the service without inputs. A job is a particular run of a
	 * workflow with given inputs, in this case empty. The returned
	 * {@link JobREST} can be checked for its status using
	 * {@link JobREST#getStatus()} and eventually it's outputs can be fetched
	 * using {@link JobREST#getOutputs()}.
	 * 
	 * @param wf
	 *            The {@link WorkflowREST} instance created using
	 *            {@link #uploadWorkflow(ScuflModel)
	 * @return The {@link JobREST} instance referencing the newly created job
	 * @throws NotSuccessException
	 *             If the job could not be created
	 */
	public JobREST addJob(WorkflowREST wf) throws NotSuccessException {
		return context.getUser().getJobs().add(wf);
	}

	/**
	 * Set a user readable title for the job taken from the {@link ScuflModel}.
	 * The title can be retrieved using {@link JobREST#getTitle()}
	 * 
	 * @param job
	 *            The job that is to be titled
	 * @param model
	 *            The model from where to extract the workflow title
	 * @throws NotSuccessException
	 *             If the title could not be set
	 */
	public void setJobTitle(JobREST job, ScuflModel model)
			throws NotSuccessException {
		job.setTitle(model.getDescription().getTitle());
	}

}
