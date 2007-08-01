package net.sf.taverna.service.backend.executor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.service.rest.client.DataREST;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.xml.StatusType;
import net.sf.taverna.tools.Bootstrap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlException;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.restlet.data.Reference;

public class RestfulExecutionThread extends Thread {

	private static Logger logger =
		Logger.getLogger(RestfulExecutionThread.class);

	private String jobUri;

	private String baseUri;

	private String workerUsername;

	private String workerPassword;

	public RestfulExecutionThread(String jobUri, String baseUri,
		String workerUsername, String workerPassword) {
		super("Restful Executor Thread");
		logger.info("Starting job execution. Job " + jobUri);
		this.jobUri = jobUri;
		this.baseUri = baseUri;
		this.workerUsername = workerUsername;
		this.workerPassword = workerPassword;
	}

	/**
	 * Serialise XML document to an InputStream.
	 * <p>
	 * This method will start a thread with a {@link PipedOutputStream} so that
	 * the serialised XML can be read from the PipedInputStream.
	 * 
	 * @param doc
	 * @return A PipedInputStream
	 * @throws IOException
	 */
	public static PipedInputStream xmlAsInputStream(final Document doc)
		throws IOException {
		PipedInputStream inputStream = new PipedInputStream();
		final PipedOutputStream outputStream =
			new PipedOutputStream(inputStream);
		Thread t = new Thread("XMLOutputter input stream pipe") {
			@Override
			public void run() {
				XMLOutputter xmlOutputter =
					new XMLOutputter(Format.getCompactFormat());
				try {
					xmlOutputter.output(doc, outputStream);
				} catch (IOException e) {
					logger.warn("Could not output XML document", e);
				}
			}
		};
		t.setDaemon(true);
		t.start();
		return inputStream;
	}

	@Override
	public void run() {
		JobREST job = getJobREST();
		WorkflowLauncher launcher = null;
		ProgressUpdaterThread updater = null;
		boolean complete = false;
		try {
			String scufl;
			try {
				scufl = job.getWorkflow().getScufl();
			} catch (RuntimeException ex) {
				logger.warn("Could not load scufl for " + job);
				return;
			}
			try {
				launcher = constructWorkflowLauncher(scufl);
			} catch (Exception ex) {
				logger.warn("Could not initiate launcher for " + job, ex);
				return;
			}
			Map<String, DataThing> inputs = new HashMap<String, DataThing>();
			if (job.getInputs() != null) {
				try {
					inputs =
						DataThingXMLFactory.parseDataDocument(new SAXBuilder().build(job.getInputs().getBaclavaStream()));
				} catch (Exception e) {
					logger.warn(
						"Could not read inputs from " + job.getInputs(), e);
				}
			}
			try {
				job.setStatus(StatusType.RUNNING);
			} catch (NotSuccessException e2) {
				logger.warn("Could not set status to running", e2); // OK for now..
			}

			GDuration updateInterval = job.getUpdateInterval();
			if (updateInterval != null) {
				updater = new ProgressUpdaterThread(launcher, job);
				updater.start();
			}

			Map outputs = null;
			try {
				outputs = launcher.execute(inputs);
			} catch (Exception e) {
				logger.warn("Workflow execution failed", e);
				return;
			}
			logger.info("Job " + job + " succeeded");
			// Stop progress report updater while we upload data
			if (updater != null) {
				updater.loop = false;
			}
			Document doc = DataThingXMLFactory.getDataDocument(outputs);
			DataREST data = null;
			try {
				data =
					job.getOwner().getDatas().add(
						new ByteArrayInputStream(
							new XMLOutputter().outputString(doc).getBytes()));
			} catch (NotSuccessException e1) {
				logger.warn("Could not upload data for job " + job, e1);
				// But we're still complete, so don't return
			}
			if (data != null) {
				try {
					job.setOutputs(data);
				} catch (NotSuccessException e) {
					logger.warn("Could not set outputs for job " + job, e);
				}
			}
			try {
				job.setStatus(StatusType.COMPLETE);
			} catch (NotSuccessException e) {
				logger.warn("Could not set complete status for job " + job, e);
			}
			complete = true;

		} finally {
			if (!complete) {
				failedJob(job);
			}
			if (launcher == null || updater == null) {
				return;
			}
			updater.loop = false;
			try {
				updater.join(); // So we're not sending two progress
				// reports at once
			} catch (InterruptedException e) {
				logger.warn(
					"Interrupted " + this + " while joining " + updater, e);
				updater.interrupt(); // take it down with us!
				Thread.currentThread().interrupt();
			}

			try {
				job.setReport(launcher.getProgressReportXML());
			} catch (NotSuccessException e) {
				logger.warn("Could not set progress report for " + job, e);
			} catch (XmlException e) {
				logger.error("Could not serialize progress report for " + job,
					e);
			}
		}
	}
	
	private void failedJob(JobREST job) {
		try {
			job.setStatus(StatusType.FAILED);
		} catch (NotSuccessException nse) {
			logger.error("Error updating job status to failed", nse);
		}
	}

	private WorkflowLauncher constructWorkflowLauncher(String scufl)
		throws MalformedURLException, ArtifactNotFoundException,
		ArtifactStateException, ClassNotFoundException, NoSuchMethodException,
		InstantiationException, IllegalAccessException,
		InvocationTargetException {
		//System.setProperty("raven.eclipse", "1");
		// FIXME: Should have a real home
		File base = new File("/tmp/");
		Set<Artifact> systemArtifacts = new HashSet<Artifact>();
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl",
			"scufl-tools", "1.5.2.0"));
		systemArtifacts.add(new BasicArtifact(
			"uk.org.mygrid.taverna.processors", "taverna-localworkers",
			"1.5.2.0"));
		systemArtifacts.add(new BasicArtifact(
			"uk.org.mygrid.taverna.processors", "taverna-java-processor",
			"1.5.2.0"));
		systemArtifacts.add(new BasicArtifact(
			"uk.org.mygrid.taverna.processors",
			"taverna-stringconstant-processor", "1.5.2.0"));

		Repository repository =
			LocalRepository.getRepository(base,
				this.getClass().getClassLoader(), systemArtifacts);
		System.setProperty("raven.profile",
			"http://www.mygrid.org.uk/taverna/updates/1.5.2/taverna-1.5.2.0-profile.xml");
		for (Artifact a : systemArtifacts) {
			repository.addArtifact(a);
		}
		// TODO: Avoid hardcoding of local test-repository!
		repository.addRemoteRepository(new URL(
			"file:/Users/stain/.m2/repository/"));
		repository.addRemoteRepository(new URL(
			"http://www.mygrid.org.uk/maven/proxy/repository/"));
		repository.addRemoteRepository(new URL(
			"http://www.mygrid.org.uk/maven/repository/"));
		TavernaSPIRegistry.setRepository(repository);
		Bootstrap.properties = new Properties();
		repository.update();

		ClassLoader cl =
			repository.getLoader(new BasicArtifact(
				"uk.org.mygrid.taverna.scufl", "scufl-tools", "1.5.2.0"),
				this.getClass().getClassLoader());
		Class workflowLauncherClass =
			cl.loadClass("org.embl.ebi.escience.scufl.tools.WorkflowLauncher");
		Constructor constructor =
			workflowLauncherClass.getConstructor(new Class[] { InputStream.class });
		ByteArrayInputStream inStream =
			new ByteArrayInputStream(scufl.getBytes());
		WorkflowLauncher launcher =
			(WorkflowLauncher) constructor.newInstance(new Object[] { inStream });
		return launcher;
	}

	private JobREST getJobREST() {
		RESTContext context = getRESTContext();
		Reference refUri = new Reference(jobUri);
		return new JobREST(context, refUri);
	}

	private RESTContext getRESTContext() {
		RESTContext context =
			new RESTContext(baseUri, workerUsername, workerPassword);
		return context;
	}
}
