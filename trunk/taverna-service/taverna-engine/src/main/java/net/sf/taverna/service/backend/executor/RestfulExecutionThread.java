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
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.restlet.data.Reference;

public class RestfulExecutionThread extends Thread {
	
	//FIXME: clearly, having the password hard-coded is not ideal!
	private final String WORKER_PASSWORD="Bob";
	
	private static Logger logger = Logger.getLogger(RestfulExecutionThread.class);
	
	private String workerUsername;
	private String jobUri;
	private String baseUri;

	public RestfulExecutionThread(String jobUri, String baseUri, String workerUsername) {
		super("Restful Executor Thread");
		logger.info("Starting job execution. Job " + jobUri);
		this.jobUri=jobUri;
		this.baseUri=baseUri;
		this.workerUsername = workerUsername;
	}
	
	/**
	 * Serialise XML document to an InputStream.
	 * <p>
	 * This method will start a thread with a {@link PipedOutputStream} so that the serialised XML
	 * can be read from the PipedInputStream.
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
		try {
			String scufl = job.getWorkflow().getScufl();
			launcher = constructWorkflowLauncher(scufl);
			Map<String,DataThing> inputs = new HashMap<String, DataThing>();
//			if (job.getInputs()!=null) {
//				inputs=job.getInputs().
//			}
			job.setStatus(StatusType.RUNNING);
			
			GDuration updateInterval = job.getUpdateInterval();
			if (updateInterval != null) {
				updater = new ProgressUpdaterThread(launcher, job);
				updater.start();
			}
			Map outputs = launcher.execute(inputs);
			if (updater != null) {
				updater.loop = false;
			}
		
			Document doc = DataThingXMLFactory.getDataDocument(outputs);
			DataREST data=job.getOwner().getDatas().add(new ByteArrayInputStream(new XMLOutputter().outputString(doc).getBytes()));
			job.setStatus(StatusType.COMPLETE);
			job.setOutputs(data);
		}
		catch(Exception e) {
			logger.warn("Workflow execution failed", e);
			try {
				job.setStatus(StatusType.FAILED);
			} catch (NotSuccessException e1) {
				logger.error("Error updating job status to failed",e1);
			}
		}
		finally {		
			if (launcher!=null) {
				
				if (updater != null) {
					updater.loop = false;
					try {
						updater.join(); // So we're not sending two progress reports at once
					} catch (InterruptedException e) {
						logger.warn("Interrupted while joining " + this, e);
						updater.interrupt(); // take it down!
					}
				}
				try {
					job.setReport(launcher.getProgressReportXML());
				} catch (NotSuccessException e) {
					logger.warn("Could not set progress report for " + job, e);
				} catch (XmlException e) {
					logger.error("Could not serialize progress report for "
						+ job, e);
				}
			}
		}
	}

	private WorkflowLauncher constructWorkflowLauncher(String scufl) throws MalformedURLException, ArtifactNotFoundException, ArtifactStateException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		System.setProperty("raven.eclipse", "1");
		// FIXME: Should have a real  home
		File base = new File("/tmp/");
		Set<Artifact> systemArtifacts = new HashSet<Artifact>();
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl","scufl-tools","1.5.2.0"));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.processors","taverna-localworkers","1.5.2.0"));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.processors","taverna-java-processor","1.5.2.0"));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.processors","taverna-stringconstant-processor","1.5.2.0"));
		
		Repository repository = LocalRepository.getRepository(base, this.getClass().getClassLoader(), systemArtifacts);
		System.setProperty("raven.profile","http://www.mygrid.org.uk/taverna/updates/1.5.2/taverna-1.5.2.0-profile.xml");
		for (Artifact a : systemArtifacts) repository.addArtifact(a);
		repository.addRemoteRepository(new URL("file:/Users/sowen/.m2/repository/"));
		repository.addRemoteRepository(new URL("http://www.mygrid.org.uk/maven/proxy/repository/"));
		repository.addRemoteRepository(new URL("http://www.mygrid.org.uk/maven/repository/"));
		TavernaSPIRegistry.setRepository(repository);
		Bootstrap.properties=new Properties();
		repository.update();
		
		ClassLoader cl = repository.getLoader(new BasicArtifact("uk.org.mygrid.taverna.scufl","scufl-tools","1.5.2.0"), this.getClass().getClassLoader());
		Class workflowLauncherClass = cl.loadClass("org.embl.ebi.escience.scufl.tools.WorkflowLauncher");
		Constructor constructor = workflowLauncherClass.getConstructor(new Class[] {InputStream.class});
		ByteArrayInputStream inStream = new ByteArrayInputStream(scufl.getBytes());
		WorkflowLauncher launcher = (WorkflowLauncher)constructor.newInstance(new Object[]{inStream});
		return launcher;
	}
	
	private JobREST getJobREST() {
		RESTContext context = getRESTContext();
		Reference refUri = new Reference(jobUri);
		return new JobREST(context,refUri);	
	}

	private RESTContext getRESTContext() {
		RESTContext context = new RESTContext(baseUri,workerUsername,WORKER_PASSWORD);
		return context;
	}
}
