package net.sf.taverna.service.wsdl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.service.backend.Engine;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.interfaces.UnknownJobException;
import net.sf.taverna.tools.Bootstrap;

import org.apache.log4j.Logger;

public class Taverna implements TavernaService {

	private static Logger logger = Logger.getLogger(Taverna.class);

	private static File tavernaHome = null;

	private static TavernaService engine = null;

	public Taverna() {
		synchronized (this) {
			if (tavernaHome == null || engine == null) {
				prepare();
			}
		}
	}

	public void reset() {
		prepare();
	}


	private synchronized void prepare() {
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
	}

	private synchronized void bootstrap() throws MalformedURLException,
		ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		Bootstrap.findUserDir();
		Bootstrap.properties = Bootstrap.findProperties();
		// Disable any download
		//		Bootstrap.remoteRepositories = Bootstrap.findRepositories(Bootstrap.properties);
		Bootstrap.remoteRepositories = new URL[0];
		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
		}
		List<URL> localLoaderUrls = new ArrayList<URL>();
		List<URL> remoteLoaderUrls = new ArrayList<URL>();
		Bootstrap.getLoaderUrls(localLoaderUrls, remoteLoaderUrls);
		Bootstrap.addSystemLoaderArtifacts();
		engine = Engine.getInstance();
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
