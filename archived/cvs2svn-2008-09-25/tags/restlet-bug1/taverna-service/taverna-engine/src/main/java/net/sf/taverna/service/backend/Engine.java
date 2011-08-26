package net.sf.taverna.service.backend;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.interfaces.UnknownJobException;
import net.sf.taverna.service.queue.QueueListener;
import net.sf.taverna.service.queue.TavernaQueue;
import net.sf.taverna.service.queue.TavernaQueueListener;
import net.sf.taverna.service.util.XMLUtils;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.ProcessorRegistry;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

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
	private static final Namespace nsXlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");

	private static Logger logger = Logger.getLogger(Engine.class);

	static {
		prepare();
		init();
	}

	private static Engine instance;

	TavernaQueue queue;

	QueueListener listener;

	Thread listenerThread;

	private static DAOFactory daoFactory = DAOFactory.getFactory();
	private static File tavernaHome;
	
	
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
		queue = new TavernaQueue();
		QueueListener listener = new TavernaQueueListener(queue);
		listenerThread = new Thread(listener);
		listenerThread.start();
	}

	@Override
	protected void finalize() {
		listener.stop();
	}

	@SuppressWarnings("deprecation")
	public static synchronized void init() {
		MyGridConfiguration.loadMygridProperties();
		Repository repository;
		try {
			LocalArtifactClassLoader acl =
				(LocalArtifactClassLoader) Engine.class.getClassLoader();
			repository = acl.getRepository();
			logger.warn("Using found artifact classloader repository");
		} catch (ClassCastException cce) {
			System.out.println("Cache is in " + Bootstrap.TAVERNA_CACHE);
			repository =
				LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE));
			logger.warn("Using fresh repository");		
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
				System.out.println("Adding repository " + remoteRepository);
			}
		}
		if (repository != null) {
			TavernaSPIRegistry.setRepository(repository);
		} else {
			System.out.println("No repository!");
		}
		if (logger.isDebugEnabled()) {
			ProcessorRegistry r = ProcessorRegistry.instance();
			logger.debug("Found processors: " + r.getProcessorInfoBeans());
		}
	}
	

	private static synchronized void prepare() {
		try {
			tavernaHome = File.createTempFile("taverna", "home");
			tavernaHome.delete();
			tavernaHome =
				new File(tavernaHome.getParentFile(), "taverna-service");
			tavernaHome.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("Can't make taverna.home");
		}

		System.err.println("Using fresh taverna.home " + tavernaHome);
		System.setProperty("taverna.home", tavernaHome.getAbsolutePath());
		System.setProperty("java.awt.headless", "true");

		System.setProperty("raven.loader.version", "1.5.1");
		// FIXME: /tmp/tavernaXhome/conf/taverna-service-profile.xml is
		// still version="1.5.1.0" name="Taverna Workbench"
		System.setProperty("raven.remoteprofile",
			"http://rpc268.cs.man.ac.uk/profiles/taverna-service-profile.xml");
		File m2Repo =
			new File(System.getProperty("user.home"), ".m2/repository");
		if (m2Repo.isDirectory()) {
			System.setProperty("raven.repository.1", m2Repo.toURI().toString());
		} else {
			System.err.println("Could not find " + m2Repo);
		}

		try {
			bootstrap();
		} catch (Throwable t) {
			System.err.println("Could not bootstrap!");
			t.printStackTrace();
		}
		System.out.println("We have initialized");
		System.out.println(Bootstrap.TAVERNA_CACHE);
	}

	public static synchronized void bootstrap() throws MalformedURLException,
		ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		Bootstrap.findUserDir();
		Bootstrap.properties = Bootstrap.findProperties();
		Bootstrap.remoteRepositories = Bootstrap.findRepositories(Bootstrap.properties);
		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
		}
		List<URL> localLoaderUrls = new ArrayList<URL>();
		List<URL> remoteLoaderUrls = new ArrayList<URL>();
		Bootstrap.getLoaderUrls(localLoaderUrls, remoteLoaderUrls);
		Bootstrap.addSystemLoaderArtifacts();
	}
	
	

	public String jobs() {
		org.jdom.Element jobsElement = new Element("jobs", ns);
		
		for (Job j : daoFactory.getJobDAO()) {
			Element jobElement = new Element("job", ns);
			jobElement.setAttribute("id", j.getId());
			jobElement.setAttribute("href", "/jobs/" +  j.getId(), nsXlink);
			
			Element statusElement = new Element("status", ns);
			jobElement.setAttribute("href", "/jobs/" + j.getId() + "/status", nsXlink);
			statusElement.addContent(j.getStatus().toString());
			jobElement.addContent(statusElement);
			jobsElement.addContent(jobElement);
		}
		daoFactory.close();
		Document document = new Document(jobsElement);
		return XMLUtils.xmlOutputter.outputString(document);
	}

	public String runWorkflowFile(String filename, String inputDoc)
		throws IOException, ParseException, QueueException {
		File workflowFile = new File(filename);
		String workflow = FileUtils.readFileToString(workflowFile, "utf8");
		return runWorkflow(workflow, inputDoc);
	}

	public String runWorkflow(String scufl, String inputDoc)
		throws IOException, ParseException, QueueException {
		Job job = queue.add(scufl, inputDoc);
		daoFactory.commit();
		daoFactory.close();
		return job.getId();
	}

	public String jobStatus(String job_id) {
		Job job;
		try {
			job = getJob(job_id);
		} catch (UnknownJobException e) {
			return "UNKNOWN";
		}
		String state = job.getStatus().toString();
		daoFactory.close();
		return state;
	}

	public String getResultDocument(String job_id) throws UnknownJobException {
    	Job job = getJob(job_id);
    	String baclava = job.getOutputDoc().getBaclava();
    	daoFactory.close();
		return baclava;
	}

	private Job getJob(String job_id) throws UnknownJobException {
		try {
			return daoFactory.getJobDAO().read(job_id);
		} catch (IllegalArgumentException ex) {
			throw new UnknownJobException(job_id);
		}
	}


	public String getProgressReport(String job_id) throws UnknownJobException {
		Job job = getJob(job_id);
		return job.getProgressReport();
	}

	public String getWorkflow(String job_id) throws UnknownJobException {
		Job job = getJob(job_id);
		return job.getWorkflow().getScufl();
	}

	public String getInputs(String job_id) throws UnknownJobException {
		Job job = getJob(job_id);
		return job.getInputs().getBaclava();
	}

	public String addJob(String username, String password, String scuflId, String baclavaId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void changePassword(String username, String password, String newPassword) {
		// TODO Auto-generated method stub
		
	}

	public String getDataDoc(String username, String password, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJob(String username, String password, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJobStatus(String username, String password, String job_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJobs(String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProgressReport(String username, String password, String job_id) throws UnknownJobException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueue(String username, String password, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueues(String username, String password, String workerURL) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResultDocument(String username, String password, String job_id) throws UnknownJobException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWorkflow(String username, String password, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String putDataDoc(String username, String password, String baclava) {
		// TODO Auto-generated method stub
		return null;
	}

	public String putWorkflow(String username, String password, String scufl) {
		// TODO Auto-generated method stub
		return null;
	}

	public void register(String username, String password, String email) {
		// TODO Auto-generated method stub
		
	}

	public void registerWorker(String username, String password, String workerURL) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterWorker(String username, String password, String workerURL) {
		// TODO Auto-generated method stub
		
	}
}
