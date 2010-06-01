package net.sf.taverna.t2.provenance;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
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
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

/**
 * Helper for testing provenance
 * 
 * @author Paolo Missier
 * @author Stian Soiland-Reyes
 */
public class ProvenanceTestHelper {

	public ProvenanceTestHelper() throws NamingException {
		setDataSource();
		makeProvenanceConnectorAndContext();
		
	}
	
	private static ApplicationRuntime applicationRuntime = ApplicationRuntime.getInstance();

	
	private static final String CONTEXT_XML = "hibernateReferenceServiceContext.xml";

	private static Logger logger = Logger.getLogger(ProvenanceTestHelper.class);

	private static Edits edits = EditsRegistry.getEdits();

	static {
		PluginManager.setRepository(ApplicationRuntime.getInstance()
				.getRavenRepository());
		PluginManager.getInstance();
	}

	private CaptureResultsListener listener;
	private WorkflowInstanceFacade facade;

	private InvocationContext context;
	private ReferenceService referenceService;
	private ProvenanceConnector provenanceConnector;
	private ProvenanceAccess pAccess = null;

	private String DB_TYPE = "derby";

	public ProvenanceConnector getProvenanceConnector() {
		return provenanceConnector;
	}

	public ProvenanceAccess getProvenanceAccess() {
		return pAccess;
	}

	// Set for mysql usage
	private String DB_USER = null;
	private String DB_PASSWD = null;
	private String DB_URL_LOCAL = null;

	private boolean DB_CLEAR_ON_STARTUP = true;

	private Connection connection;

	/**
	 * @return the context
	 */
	public InvocationContext getContext() {
		return context;
	}

	/**
	 * @return the facade
	 */
	public WorkflowInstanceFacade getFacade() {
		return facade;
	}

	/**
	 * @return the listener
	 */
	public CaptureResultsListener getListener() {
		return listener;
	}

	/**
	 * @return the referenceService
	 */
	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public Dataflow loadDataflow(String workflowName) throws IOException,
			JDOMException, DeserializationException, EditException {
		String resourceName = "/workflows/" + workflowName + ".t2flow";
		InputStream inStream = getClass().getResourceAsStream(resourceName); //$NON-NLS-1$
		if (inStream == null) {
			throw new IOException(
					"Unable to find resource for t2 dataflow:" + resourceName); //$NON-NLS-1$
		}
		return loadDataflow(inStream);
	}

	public Dataflow loadDataflow(InputStream inStream) throws JDOMException,
			IOException, DeserializationException, EditException {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		SAXBuilder builder = new SAXBuilder();
		Element el = builder.build(inStream).detachRootElement();
		return deserializer.deserializeDataflow(el);
	}

	public void makeProvenanceConnectorAndContext() {
		if (DB_TYPE.equals("mysql")) {
			pAccess = new ProvenanceAccess(ProvenanceConnectorType.MYSQL);
		} else {
			pAccess = new ProvenanceAccess(ProvenanceConnectorType.DERBY);
		}
		provenanceConnector = pAccess.getProvenanceConnector();

		assertNotNull("Could not find provenanceConnector", provenanceConnector);

		referenceService = provenanceConnector.getReferenceService();
		
		context = new InvocationContextImpl(referenceService,
				provenanceConnector);
		assertNotNull("Could not make context", context);
		provenanceConnector.setReferenceService(referenceService);
		provenanceConnector.setInvocationContext(context);
		
		
		provenanceConnector.clearDatabase(DB_CLEAR_ON_STARTUP);
		// Only clear once
		DB_CLEAR_ON_STARTUP = false;
	}

	public Dataflow prepareDataflowRun(String dataflowFile) throws IOException,
			JDOMException, DeserializationException, EditException,
			InvalidDataflowException {
		Dataflow dataflow = null;
		// Reset provenance connector
		provenanceConnector.init();
		logger.info("Preparing to run workflow:" + dataflowFile);
		dataflow = loadDataflow(dataflowFile);
		dataflow.checkValidity();

		facade = edits.createWorkflowInstanceFacade(dataflow, context, ""); //$NON-NLS-1$
		listener = new CaptureResultsListener(dataflow, context);
		facade.addResultListener(listener);
		facade.fire();
		return dataflow;

	}

	public void setDataSource() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.osjava.sj.memory.MemoryContextFactory");
		System.setProperty("org.osjava.sj.jndi.shared", "true");

		BasicDataSource ds = new BasicDataSource();

		if (DB_TYPE.equals("mysql")) {
			ds.setDriverClassName("com.mysql.jdbc.Driver");
		} else {
			ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		}
		ds.setDefaultTransactionIsolation(
				Connection.TRANSACTION_READ_UNCOMMITTED);
		ds.setMaxActive(50);
		ds.setMinIdle(10);
		ds.setMaxIdle(50);
		ds.setDefaultAutoCommit(true);
		if (DB_TYPE.equals("mysql")) {
			ds.setUsername(DB_USER);
			ds.setPassword(DB_PASSWD);
			ds.setUrl("jdbc:mysql://" + DB_URL_LOCAL + "/T2Provenance");
		} else {
			ds.setUrl("jdbc:derby:" + applicationRuntime.getApplicationHomeDir() + "t2-database;create=true;upgrade=true");
		}

		InitialContext context = new InitialContext();
		context.rebind("jdbc/taverna", ds);

	}

	/**
	 * @param facade
	 *            the facade to set
	 */
	public void setFacade(WorkflowInstanceFacade facade) {
		this.facade = facade;
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(CaptureResultsListener listener) {
		this.listener = listener;
	}

	public void waitForCompletion() throws InterruptedException,
			DataflowTimeoutException {
		waitForCompletion(listener);
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

	public Connection getConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (connection == null) {
			connection = getProvenanceConnector().getQuery().getConnection();
		}
		return connection;
	}

}
