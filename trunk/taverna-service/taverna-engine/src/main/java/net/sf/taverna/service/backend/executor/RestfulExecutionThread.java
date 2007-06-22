package net.sf.taverna.service.backend.executor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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

	public RestfulExecutionThread(String jobUri, String baseUri,String workerUsername) {
		super("Restful Executor Thread");
		logger.info("Starting job execution. JobURI="+jobUri);
		this.jobUri=jobUri;
		this.baseUri=baseUri;
		this.workerUsername = workerUsername;
	}
	
	@Override
	public void run() {
		JobREST job = getJobREST();
		WorkflowLauncher launcher = null;
		try {
			String scufl = job.getWorkflow().getScufl();
			job.setStatus(StatusType.RUNNING);
			
			launcher = constructWorkflowLauncher(scufl);
			Map<String,DataThing> inputs = new HashMap<String, DataThing>();
//			if (job.getInputs()!=null) {
//				inputs=job.getInputs().
//			}
			Map outputs = launcher.execute(inputs);
			
			//FIXME: uploads as a stream. Fairly pointless at the moment, since the outputs
			//are held in memory before creating the stream (and are held in memory again on the server side)
			//, but at least the transport is correct!
			Document doc = DataThingXMLFactory.getDataDocument(outputs);
			String baclavaString = new XMLOutputter(Format.getCompactFormat()).outputString(doc);
			ByteArrayInputStream inStream = new ByteArrayInputStream(baclavaString.getBytes());
			DataREST data=job.getOwner().getDatas().add(inStream);
			job.setStatus(StatusType.COMPLETE);
			job.setOutputs(data);
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				job.setStatus(StatusType.FAILED);
			} catch (NotSuccessException e1) {
				logger.error("Error updating job status to failed",e1);
			}
		}
		finally {
			if (launcher!=null) {
				try {
					job.setReport(launcher.getProgressReportXML());
				} catch (NotSuccessException e) {
					logger.error(e);
				} catch (XmlException e) {
					logger.error(e);
				}
			}
		}
	}

	private WorkflowLauncher constructWorkflowLauncher(String scufl) throws MalformedURLException, ArtifactNotFoundException, ArtifactStateException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		System.setProperty("raven.eclipse", "1");
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
