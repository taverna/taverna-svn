package net.sf.taverna.service.backend;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.interfaces.UnknownJobException;
import net.sf.taverna.service.queue.Job;
import net.sf.taverna.service.queue.QueueListener;
import net.sf.taverna.service.queue.TavernaQueue;
import net.sf.taverna.service.queue.TavernaQueueListener;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflworkers.ProcessorRegistry;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Backend for Taverna service.
 * <p>
 * Proxied to by {@link net.sf.taverna.service.wsdl.Taverna} or other possible
 * transport types. Implements the {@link TavernaService} interface.
 * <p>
 * The idea is that the backend could be loaded through Raven, while the
 * frontend is outside Raven and loaded by the container. The front end can then
 * use Raven to fetch the backend, giving the possibilites for living "inside
 * Raven" even as a web service. (It would be more difficult to launch the
 * container using Raven). Note however that this approach is no longer used, as
 * this caused issues with profiles, plugins, etc., in addition to problems with
 * classes (such as {@link TavernaService}) being on both the inside and
 * outside of Raven.
 * 
 * @author Stian Soiland
 */
public class Engine implements TavernaService {

	private static final Namespace ns = Namespace.getNamespace(NS);

	private static Logger logger = Logger.getLogger(Engine.class);

	static {
		init();
	}

	private static Engine instance;

	Map<String, Job> jobs;

	TavernaQueue queue;

	QueueListener listener;

	Thread listenerThread;

	// FIXME: Use Format.getCompactFormat()
	private static XMLOutputter xmlOutputter =
		new XMLOutputter(Format.getPrettyFormat());

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
	 * Private constructor, use singleton accessor {@link #getInstance()}
	 * instead.
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
			LocalArtifactClassLoader acl =
				(LocalArtifactClassLoader) Engine.class.getClassLoader();
			repository = acl.getRepository();
			logger.warn("Using found artifact classloader repository");
		} catch (ClassCastException cce) {
			repository =
				LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE));
			logger.warn("Using fresh repository " + repository);
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
			}
		}
		if (repository != null) {
			TavernaSPIRegistry.setRepository(repository);
		}
		if (logger.isDebugEnabled()) {
			ProcessorRegistry r = ProcessorRegistry.instance();
			logger.debug("Found processors: " + r.getProcessorInfoBeans());
		}
	}

	public String jobs() {
		org.jdom.Element jobsElement = new Element("jobs", ns);
		for (Entry<String, Job> e : jobs.entrySet()) {
			Element jobElement = new Element("job", ns);
			jobElement.setAttribute("id", e.getKey());
			Element statusElement = new Element("status", ns);
			statusElement.addContent(e.getValue().getState().toString());
			jobElement.addContent(statusElement);
			jobsElement.addContent(jobElement);
		}
		Document document = new Document(jobsElement);
		return xmlOutputter.outputString(document);
	}

	public String runWorkflowFile(String filename, String inputDoc)
		throws IOException, QueueException {
		File workflowFile = new File(filename);
		String workflow = FileUtils.readFileToString(workflowFile, "utf8");
		return runWorkflow(workflow, inputDoc);
	}

	public String runWorkflow(String scufl, String inputDoc)
		throws IOException, QueueException {
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

	public String getResultDocument(String job_id) throws UnknownJobException {
		Job job = jobs.get(job_id);
		if (job == null) {
			throw new UnknownJobException(job_id);
		}
		Map<String, DataThing> results = job.getResults();
		return makeDataDocument(results);
	}

	private String makeDataDocument(Map<String, DataThing> values) {
		org.jdom.Document doc = DataThingXMLFactory.getDataDocument(values);
		String xmlString = xmlOutputter.outputString(doc);
		return xmlString;
	}

	public String getProgressReport(String job_id) throws UnknownJobException {
		Job job = jobs.get(job_id);
		if (job == null) {
			throw new UnknownJobException(job_id);
		}
		return job.getProgressReport();
	}

	public String getWorkflow(String job_id) throws UnknownJobException {
		Job job = jobs.get(job_id);
		if (job == null) {
			throw new UnknownJobException(job_id);
		}
		ScuflModel workflow = job.getWorkflow();
		return XScuflView.getXMLText(workflow);
	}

	public String getInputs(String job_id) throws UnknownJobException {
		Job job = jobs.get(job_id);
		if (job == null) {
			throw new UnknownJobException(job_id);
		}
		Map<String, DataThing> inputs = job.getInputs();
		return makeDataDocument(inputs);
	}
}
