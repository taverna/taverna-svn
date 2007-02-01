package net.sf.taverna.service.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.service.queue.Job;
import net.sf.taverna.service.queue.QueueException;
import net.sf.taverna.service.queue.QueueListener;
import net.sf.taverna.service.queue.TavernaQueue;
import net.sf.taverna.service.queue.TavernaQueueListener;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.io.FileUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;


public class Engine {
	
	static {
		init();
	}

	private static Engine instance;
	Map<String, Job> jobs;	
	TavernaQueue queue;
	QueueListener listener;
	Thread listenerThread;	
	
	/**
	 * Get the Engine singleton.
	 * 
	 * @return The Engine singleton instance
	 */
	public static Engine getInstance() {
		if (instance == null) {
			instance = new Engine();
		}
		return instance;
	}
	
	/**
	 * Private constructor, use singleton accessor {@link #getInstance()} instead. 
	 *
	 */
	private Engine() {
		jobs = new Hashtable<String, Job>();
		queue = new TavernaQueue();
		QueueListener listener = new TavernaQueueListener(queue);
		listenerThread = new Thread(listener);
		listenerThread.start();
	}	
	
	protected void finalize() {
		listener.stop();
	}
	
	
	@SuppressWarnings("deprecation")
	private static void init() {
		MyGridConfiguration.loadMygridProperties();
		
		Repository repository;
		try {
			LocalArtifactClassLoader acl = (LocalArtifactClassLoader) Engine.class.getClassLoader();
			repository = acl.getRepository();
			System.out.println("Using found artifact classloader repository");
		} catch (ClassCastException cce) {
			repository = LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE));
			System.out.println("Using fresh repository " + repository);
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
			}
		}
		if (repository != null) {
			TavernaSPIRegistry.setRepository(repository);
		}
	}
	
	public String ping(String msg) {
		System.out.println("ping " + msg);
		return "pong: " + msg;
	}
	
	@SuppressWarnings("unchecked")
	public String executeWorkflow(String filename) throws WorkflowSubmissionException, InvalidInputException, FileNotFoundException, ProcessorCreationException, DataConstraintCreationException, UnknownProcessorException, UnknownPortException, DuplicateProcessorNameException, MalformedNameException, ConcurrencyConstraintCreationException, DuplicateConcurrencyConstraintNameException, XScuflFormatException, IOException {
		File workflow = new File(filename);
		URL url = workflow.toURI().toURL();
		WorkflowLauncher l = new WorkflowLauncher(url);
		Map<String, DataThing> outputs = l.execute(new HashMap());
		
		Document doc = DataThingXMLFactory.getDataDocument(outputs);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String outputDoc = xo.outputString(doc);	
		
		String report = l.getProgressReportXML();
		// FIXME: Not really valid XML
		return outputDoc + "\n" + report;
	}
	
	public String runWorkflowFile(String filename, String inputDoc) throws IOException, QueueException {
		File workflowFile = new File(filename);
		String workflow = FileUtils.readFileToString(workflowFile, "utf8");
		return runWorkflow(workflow, inputDoc);
	}
	
	public String runWorkflow(String scufl, String inputDoc) throws IOException, QueueException {
		Job job = queue.add(scufl, inputDoc);
		jobs.put(job.id, job);
		return job.id;
	}
	
	public String jobStatus(String job_id) {
		Job job = jobs.get(job_id);	
		if (job == null) {
			return "UNKNOWN";
		}
		return job.getState().toString();
	}
	public String getResultDocument(String job_id) {
		Job job = jobs.get(job_id);
		Map<String, DataThing> results = job.getResults();
		Document doc = DataThingXMLFactory.getDataDocument(results);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String xmlString = xo.outputString(doc);	
		return xmlString;
	}
	
	public String getProgressReport(String job_id) {
		Job job = jobs.get(job_id);
		return job.getProgressReport();
	}
	
	public String getProfile(String ignore) {
		return ""+ProfileFactory.getInstance().getProfile().getArtifacts();
	}
	
}
