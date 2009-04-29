package net.sf.taverna.t2.lineageService.capture.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.connector.DerbyProvenanceConnector;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.WorkflowDataProcessor;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
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
 * 
 * @author Paolo Missier
 */
public class DerbyProvenanceCaptureTestHelper {

	private CaptureResultsListener listener;
	private WorkflowInstanceFacade facade;

	protected InvocationContext context;
	private ReferenceService referenceService;
	private ProvenanceConnector provenanceConnector;

	private String DB_URL_LOCAL = testFiles.getString("dbhost"); // URL of database server //$NON-NLS-1$
	private String DB_USER = testFiles.getString("dbuser"); // database user id //$NON-NLS-1$
	private String DB_PASSWD = testFiles.getString("dbpassword"); //$NON-NLS-1$

	// testing switches
	private String clearDB = testFiles.getString("clearDB");
	private String saveEvents = testFiles.getString("saveEvents");
	boolean isClearDB = false;
	boolean isUseProvenance = true;

	@SuppressWarnings("unchecked")//$NON-NLS-1$
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

		referenceService = (ReferenceService) appContext
				.getBean("t2reference.service.referenceService"); //$NON-NLS-1$

		if (clearDB != null)
			isClearDB = Boolean.parseBoolean(clearDB);

		// String jdbcString = "jdbc:mysql://" + DB_URL_LOCAL +
		// "/T2Provenance?user="
		// + DB_USER + "&password=" + DB_PASSWD;
		// derby specific bit here
		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "provenance");
		try {
			FileUtils.forceMkdir(dbFile);
		} catch (IOException e2) {

		}
		String jdbcString = "jdbc:derby:" + dbFile.toString()
				+ "/db;create=true;upgrade=true";

		ProvenanceWriter writer = new DerbyProvenanceWriter();
		writer.setDbURL(jdbcString);
		try {
			writer.clearDBStatic();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProvenanceQuery query = new DerbyProvenanceQuery();
		query.setDbURL(jdbcString);
		WorkflowDataProcessor wfdp = new WorkflowDataProcessor();
		wfdp.setPq(query);
		wfdp.setPw(writer);
		
		EventProcessor eventProcessor = new EventProcessor();
		eventProcessor.setPw(writer);
		eventProcessor.setPq(query);
		eventProcessor.setWfdp(wfdp);
		ProvenanceAnalysis provenanceAnalysis = null;
		try {
			provenanceAnalysis = new ProvenanceAnalysis(query);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Provenance provenance = new Provenance(eventProcessor, jdbcString);
		provenanceConnector = new DerbyProvenanceConnector(provenance, provenanceAnalysis,
				jdbcString, isClearDB, saveEvents);
		provenanceConnector.setReferenceService(referenceService);
		provenanceConnector.createDatabase();
		provenanceConnector.setInvocationContext(context);

		context =  new InvocationContext() {

			public ReferenceService getReferenceService() {
				return referenceService;
			}

			public ProvenanceReporter getProvenanceReporter() {
				if (isUseProvenance)
					return (ProvenanceReporter) provenanceConnector;
				return null;
			}

			public <T> List<? extends T> getEntities(Class<T> entityType) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		provenanceConnector.setReferenceService(context.getReferenceService());

	}

	protected Dataflow loadDataflow(String resourceName) throws Exception {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		InputStream inStream = DerbyProvenanceCaptureTestHelper.class
				.getResourceAsStream("/provenance-testing/" + resourceName); //$NON-NLS-1$
		if (inStream == null)
			throw new IOException(
					"Unable to find resource for t2 dataflow:" + resourceName); //$NON-NLS-1$
		SAXBuilder builder = new SAXBuilder();
		Element el = builder.build(inStream).detachRootElement();
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
	protected void waitForCompletion(CaptureResultsListener listener)
			throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(listener, 3000);
	}

	protected void waitForCompletion(CaptureResultsListener listener,
			int maxtimeSeconds) throws InterruptedException,
			DataflowTimeoutException {

		float time = 0;
		int maxTime = maxtimeSeconds * 1000;
		int interval = 100;
		while (!listener.isFinished()) {
			Thread.sleep(interval);
			time += interval;
			if (time > maxTime) {
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

	// / Paolo's code from here
	static {
		PluginManager.setRepository(ApplicationRuntime.getInstance()
				.getRavenRepository());
		PluginManager.getInstance();
	}

	public Dataflow setup(String testfilesProperty) throws Exception {

		String T2File = testFiles.getString(testfilesProperty);

		String useProvenance = testFiles.getString("useProvenance");

		if (useProvenance != null)
			isUseProvenance = Boolean.parseBoolean(useProvenance);
		System.out.println("enable provenance: " + isUseProvenance);

		Dataflow dataflow = null;

		makeDataManager();

		System.out.println("input workflow: [" + T2File + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		dataflow = loadDataflow(T2File);

		dataflow.checkValidity();
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,
				context, ""); //$NON-NLS-1$
		listener = new CaptureResultsListener(dataflow, context);
		facade.addResultListener(listener);

		facade.fire();

		return dataflow;
	}

	public void waitForCompletion() throws InterruptedException,
			DataflowTimeoutException {
		waitForCompletion(listener);
	}

	/**
	 * @return the listener
	 */
	public CaptureResultsListener getListener() {
		return listener;
	}

	/**
	 * @param listener
	 *            the listener to set
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
	 * @param facade
	 *            the facade to set
	 */
	public void setFacade(WorkflowInstanceFacade facade) {
		this.facade = facade;
	}

	/**
	 * @return the context
	 */
	public InvocationContext getContext() {
		return context;
	}

	/**
	 * @return the referenceService
	 */
	public ReferenceService getReferenceService() {
		return referenceService;
	}

}
