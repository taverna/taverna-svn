package net.sf.taverna.t2.lineageService.capture.test;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lineageService.capture.test.CaptureResultsListener;
import net.sf.taverna.t2.lineageService.capture.test.DataflowTimeoutException;
import net.sf.taverna.t2.lineageService.capture.test.DummyEventHandler;
import net.sf.taverna.t2.lineageService.capture.test.testFiles;
import net.sf.taverna.t2.provenance.connector.MySQLProvenanceConnector;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.IntermediateProvenance;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.springframework.context.ApplicationContext;


/**
 * provides common code that loads and sets up the dataflow prior to invocation<br/>
 * other test classes differ simply in the inputs they supply to the dataflow 
 * @author Paolo Missier
 */
public class ProvenanceCaptureTestHelper {

	private CaptureResultsListener listener;
	private WorkflowInstanceFacade facade;

	protected InvocationContext context;
	private ReferenceService referenceService;
	private ProvenanceConnector provenanceConnector;
	
	private String DB_URL_LOCAL = testFiles.getString("dbhost");  // URL of database server //$NON-NLS-1$
	private String DB_USER = testFiles.getString("dbuser");                        // database user id //$NON-NLS-1$
	private String DB_PASSWD = testFiles.getString("dbpassword"); //$NON-NLS-1$
	
	// testing switches
	private String clearDB = testFiles.getString("clearDB");
	private String saveEvents = testFiles.getString("saveEvents");
	boolean isClearDB = false;
	
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	
	public void createEventsDir() {
		
		try {
			FileUtils.forceMkdir(new File("/tmp/TEST-EVENTS"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	@Before	
	public void makeDataManager() {
		
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
		"inMemoryIntegrationTestsContext.xml"); //$NON-NLS-1$
		
		referenceService = (ReferenceService) appContext.getBean("t2reference.service.referenceService"); //$NON-NLS-1$
		
		if (clearDB != null)  isClearDB = Boolean.parseBoolean(clearDB);

		provenanceConnector = new MySQLProvenanceConnector();
		provenanceConnector.setClearDB(isClearDB);
		((MySQLProvenanceConnector)provenanceConnector).setSaveEvents(saveEvents);
		provenanceConnector.setUser(DB_USER);
		provenanceConnector.setPassword(DB_PASSWD);
		provenanceConnector.setDBLocation(DB_URL_LOCAL);
		provenanceConnector.init();
		provenanceConnector.setReferenceService(referenceService);
		
		
		context =  new InvocationContext() {

			public ReferenceService getReferenceService() {
				return referenceService;
			}

			public <T> List<? extends T> getEntities(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			public ProvenanceConnector getProvenanceConnector() {
				return provenanceConnector;
				//return null;
			}
		};
	}

	
	
	protected Dataflow loadDataflow(String resourceName) throws Exception {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		InputStream inStream = ProvenanceCaptureTestHelper.class.getResourceAsStream("/provenance-testing/" + resourceName); //$NON-NLS-1$
		if (inStream==null) throw new IOException("Unable to find resource for t2 dataflow:"+resourceName); //$NON-NLS-1$
		SAXBuilder builder = new SAXBuilder();
		Element el= builder.build(inStream).detachRootElement();
		return deserializer.deserializeDataflow(el);
	}
	
	
	/**
	 * 
	 * Uses a default max time of 30 seconds
	 * 
	 * @param listener
	 * @throws InterruptedException
	 * @throws DataflowTimeoutException
	 */
	protected void waitForCompletion(CaptureResultsListener listener) throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(listener, 3000);
	}
	
	protected void waitForCompletion(CaptureResultsListener listener,int maxtimeSeconds) throws InterruptedException, DataflowTimeoutException{
		float time=0;
		int maxTime = maxtimeSeconds*1000;
		int interval=100;
		while (!listener.isFinished()) {
			Thread.sleep(interval);
			time+=interval;
			if (time>maxTime) {
				throw new DataflowTimeoutException("The max time of " //$NON-NLS-1$
						+ maxtimeSeconds
						+ "s was exceed waiting for the results"); //$NON-NLS-1$
			}
		}
	}
	protected void waitForCompletion(
			Map<String, DummyEventHandler> eventHandlers, int maxtimeSeconds)
			throws InterruptedException, DataflowTimeoutException {
		int time = 0;
		boolean finished = false;
		while (!finished) {
			finished = true;
			for (DummyEventHandler testEventHandler : eventHandlers.values()) {
				if (testEventHandler.getResult() == null) {
					finished = false;
					Thread.sleep(1000);
					time += 1000;
					break;
				}
				if (time > (maxtimeSeconds * 1000))
					throw new DataflowTimeoutException("The max time of " //$NON-NLS-1$
							+ maxtimeSeconds
							+ "s was exceed waiting for the results"); //$NON-NLS-1$
			}
		}
	}
	
	/// Paolo's code from here
	static {
		PluginManager.setRepository(ApplicationRuntime.getInstance().getRavenRepository());
		PluginManager.getInstance();
	}
	
	public Dataflow setup(String testfilesProperty) throws Exception {
	
		String T2File = testFiles.getString(testfilesProperty);

		Dataflow dataflow = null;

		makeDataManager();

		System.out.println("input workflow: ["+T2File+"]"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			dataflow = loadDataflow(T2File);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataflow.checkValidity();
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,""); //$NON-NLS-1$
		listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);

		facade.fire();

		return dataflow;
	}


	public void waitForCompletion() throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(listener);	
	}

	/**
	 * @return the listener
	 */
	public CaptureResultsListener getListener() {
		return listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(CaptureResultsListener listener) {
		this.listener = listener;
	}


	/**
	 * @return the facade
	 */
	public WorkflowInstanceFacade getFacade() {
		return facade;
	}


	/**
	 * @param facade the facade to set
	 */
	public void setFacade(WorkflowInstanceFacade facade) {
		this.facade = facade;
	}




}


