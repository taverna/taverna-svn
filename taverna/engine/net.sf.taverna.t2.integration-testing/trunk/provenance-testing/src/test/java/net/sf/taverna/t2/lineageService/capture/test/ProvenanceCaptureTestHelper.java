package net.sf.taverna.t2.lineageService.capture.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

/**
 * provides common code that loads and sets up the dataflow prior to invocation<br/>
 * other test classes differ simply in the inputs they supply to the dataflow
 * 
 * @author Paolo Missier
 */
public class ProvenanceCaptureTestHelper {

	private static Logger logger = Logger.getLogger(ProvenanceCaptureTestHelper.class);

	private CaptureResultsListener listener;
	private WorkflowInstanceFacade facade;

	protected InvocationContext context;
	private ReferenceService referenceService;
	private ProvenanceConnector provenanceConnector;
	private ProvenanceAccess pAccess = null;

	private String DB_URL_LOCAL = propertiesReader.getString("dbhost"); // URL of database server //$NON-NLS-1$
	private String DB_USER = propertiesReader.getString("dbuser"); // database user id //$NON-NLS-1$
	private String DB_PASSWD = propertiesReader.getString("dbpassword"); //$NON-NLS-1$
	private String DB_TYPE = propertiesReader.getString("dbtype"); 
	
	// testing switches
	private String clearDB = propertiesReader.getString("clearDB");
	private String saveEvents = propertiesReader.getString("saveEvents");
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


	public void setDataSource() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.osjava.sj.memory.MemoryContextFactory");
		System.setProperty("org.osjava.sj.jndi.shared", "true");

		BasicDataSource ds = new BasicDataSource();
		
		if (DB_TYPE.equals("mysql")) {
			ds.setDriverClassName("com.mysql.jdbc.Driver");
		} else {
			ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		}
		ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		ds.setMaxActive(50);
		ds.setMinIdle(10);
		ds.setMaxIdle(50);
		ds.setDefaultAutoCommit(true);
		if (DB_TYPE.equals("mysql")) {
			ds.setUsername(DB_USER);
			ds.setPassword(DB_PASSWD);
			ds.setUrl("jdbc:mysql://"+DB_URL_LOCAL+"/T2Provenance");
		} else {
			ds.setUrl("jdbc:derby:t2-database;create=true;upgrade=true");
		}

	
		InitialContext context = new InitialContext();
		context.rebind("jdbc/taverna", ds);
		
	}



	public void makeDataManager() throws NamingException {	
		setDataSource();
		if (DB_TYPE.equals("mysql")) {
			pAccess = new ProvenanceAccess(ProvenanceConnectorType.MYSQL);  // creates and initializes the provenance API		
		} else {			
			pAccess = new ProvenanceAccess(ProvenanceConnectorType.DERBY);  // creates and initializes the provenance API
		}
		provenanceConnector = pAccess.getProvenanceConnector();  // oc is initialized at this point
		
		// clear DB if user so chooses
		if (clearDB != null) isClearDB = Boolean.parseBoolean(clearDB);
		provenanceConnector.clearDatabase(isClearDB);

//		ProvenanceQuery query         = provenanceConnector.getQuery();
//		ProvenanceWriter writer       = provenanceConnector.getWriter();
//		WorkflowDataProcessor wfdp    = provenanceConnector.getWfdp();

		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
		"inMemoryIntegrationTestsContext.xml"); //$NON-NLS-1$

		referenceService = (ReferenceService) appContext
		.getBean("t2reference.service.referenceService"); //$NON-NLS-1$

		context =  new InvocationContextImpl(referenceService, provenanceConnector);
		provenanceConnector.setReferenceService(context.getReferenceService()); // CHECK context.getReferenceService());
		provenanceConnector.setInvocationContext(context);

//		try {
//			provenanceConnector.getWriter().clearDBStatic();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	
	
	protected Dataflow loadDataflow(String resourceName) throws Exception {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		InputStream inStream = ProvenanceCaptureTestHelper.class
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

		String T2File = propertiesReader.getString(testfilesProperty);

		String useProvenance = propertiesReader.getString("useProvenance");

		if (useProvenance != null)
			isUseProvenance = Boolean.parseBoolean(useProvenance);
		logger.info("enable provenance: " + isUseProvenance);

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
