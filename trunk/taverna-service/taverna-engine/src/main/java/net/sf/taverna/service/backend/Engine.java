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
import net.sf.taverna.service.interfaces.TavernaConstants;
import net.sf.taverna.service.interfaces.UnknownJobException;
import net.sf.taverna.service.queue.QueueListener;
import net.sf.taverna.service.queue.TavernaQueue;
import net.sf.taverna.service.queue.TavernaQueueListener;
import net.sf.taverna.service.util.XMLUtils;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.tools.RavenProperties;
import net.sf.taverna.tools.Repositories;
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
 * transport types. Implements the {@link TavernaConstants} interface.
 * <p>
 * The idea is that the backend could be loaded through Raven, while the
 * frontend is outside Raven and loaded by the container. The front end can then
 * use Raven to fetch the backend, giving the possibilites for living "inside
 * Raven" even as a web service. (It would be more difficult to launch the
 * container using Raven). Note however that this approach is no longer used, as
 * this caused issues with profiles, plugins, etc., in addition to problems with
 * classes (such as {@link TavernaConstants}) being on both the inside and
 * outside of Raven.
 * 
 * @author Stian Soiland
 */
//public class Engine implements TavernaConstants {
//
//	private static final Namespace ns = Namespace.getNamespace(NS);
//	private static final Namespace nsXlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");
//
//	private static Logger logger = Logger.getLogger(Engine.class);
//
//	static {
//		prepare();
//		init();
//	}
//
//	private static Engine instance;
//
//	TavernaQueue queue;
//
//	QueueListener listener;
//
//	Thread listenerThread;
//
//	private static DAOFactory daoFactory = DAOFactory.getFactory();
//	private static File tavernaHome;
//	
//	
//	/**
//	 * Get the Engine singleton.
//	 * 
//	 * @return The Engine singleton instance
//	 */
//	public static Engine getInstance() {
//		if (instance == null) {
//			instance = new Engine();
//		}
//		return instance;
//	}
//
//	/**
//	 * Private constructor, use singleton accessor {@link #getInstance()}
//	 * instead.
//	 */
//	private Engine() {
//		queue = new TavernaQueue();
//		QueueListener listener = new TavernaQueueListener(queue);
//		listenerThread = new Thread(listener);
//		listenerThread.start();
//	}
//
//	@Override
//	protected void finalize() {
//		listener.stop();
//	}
//
//	@SuppressWarnings("deprecation")
//	public static synchronized void init() {
//		MyGridConfiguration.loadMygridProperties();
//		Repository repository;
//		try {
//			LocalArtifactClassLoader acl =
//				(LocalArtifactClassLoader) Engine.class.getClassLoader();
//			repository = acl.getRepository();
//			logger.warn("Using found artifact classloader repository");
//		} catch (ClassCastException cce) {
//			System.out.println("Cache is in " + Bootstrap.TAVERNA_CACHE);
//			repository =
//				LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE));
//			logger.warn("Using fresh repository");		
//			for (URL remoteRepository : Bootstrap.remoteRepositories) {
//				repository.addRemoteRepository(remoteRepository);
//				System.out.println("Adding repository " + remoteRepository);
//			}
//		}
//		if (repository != null) {
//			TavernaSPIRegistry.setRepository(repository);
//		} else {
//			System.out.println("No repository!");
//		}
//		if (logger.isDebugEnabled()) {
//			ProcessorRegistry r = ProcessorRegistry.instance();
//			logger.debug("Found processors: " + r.getProcessorInfoBeans());
//		}
//	}
//	
//
//	private static synchronized void prepare() {
//		try {
//			tavernaHome = File.createTempFile("taverna", "home");
//			tavernaHome.delete();
//			tavernaHome =
//				new File(tavernaHome.getParentFile(), "taverna-service");
//			tavernaHome.mkdir();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new NullPointerException("Can't make taverna.home");
//		}
//
//		System.err.println("Using fresh taverna.home " + tavernaHome);
//		System.setProperty("taverna.home", tavernaHome.getAbsolutePath());
//		System.setProperty("java.awt.headless", "true");
//
//		System.setProperty("raven.loader.version", "1.5.1");
//		// FIXME: /tmp/tavernaXhome/conf/taverna-service-profile.xml is
//		// still version="1.5.1.0" name="Taverna Workbench"
//		System.setProperty("raven.remoteprofile",
//			"http://rpc268.cs.man.ac.uk/profiles/taverna-service-profile.xml");
//		File m2Repo =
//			new File(System.getProperty("user.home"), ".m2/repository");
//		if (m2Repo.isDirectory()) {
//			System.setProperty("raven.repository.1", m2Repo.toURI().toString());
//		} else {
//			System.err.println("Could not find " + m2Repo);
//		}
//
//		try {
//			bootstrap();
//		} catch (Throwable t) {
//			System.err.println("Could not bootstrap!");
//			t.printStackTrace();
//		}
//		System.out.println("We have initialized");
//		System.out.println(Bootstrap.TAVERNA_CACHE);
//	}
//
//	public static synchronized void bootstrap() throws MalformedURLException,
//		ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
//		Bootstrap.findUserDir();
//		Bootstrap.properties = RavenProperties.getInstance().getProperties();
//		Bootstrap.remoteRepositories = new Repositories().find();
//		List<URL> localLoaderUrls = new ArrayList<URL>();
//		List<URL> remoteLoaderUrls = new ArrayList<URL>();
//		Bootstrap.getLoaderUrls(localLoaderUrls, remoteLoaderUrls);
//		Bootstrap.addSystemLoaderArtifacts();
//	}
//	
//	
//}
