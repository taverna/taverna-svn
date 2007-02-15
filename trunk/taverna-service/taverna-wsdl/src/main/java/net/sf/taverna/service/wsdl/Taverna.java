package net.sf.taverna.service.wsdl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.service.backend.Engine;
import net.sf.taverna.service.interfaces.QueueException;
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

	public String runWorkflow(String scufl, String inputDoc) throws QueueException, IOException {
		return engine.runWorkflow(scufl, inputDoc);
	}

	public String runWorkflowFile(String filename, String inputDoc) throws QueueException, IOException {
		return engine.runWorkflowFile(filename, inputDoc);
	}

	public String jobStatus(String job_id) {
		return engine.jobStatus(job_id);
	}

	public String jobs() {
		return engine.jobs();
	}

	public String getResultDocument(String job_id) throws UnknownJobException {
		return engine.getResultDocument(job_id);
	}

	public String getProgressReport(String job_id) throws UnknownJobException {
		return engine.getProgressReport(job_id);
	}

	public String getWorkflow(String job_id) throws UnknownJobException {
		return engine.getWorkflow(job_id);
	}

	public String getInputs(String job_id) throws UnknownJobException {
		return engine.getInputs(job_id);
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
}
