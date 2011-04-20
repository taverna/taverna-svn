/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.api.Query;
import net.sf.taverna.t2.provenance.client.XMLQuery.ProvenanceQueryParser;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.impl.T2ReferenceImpl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 * @author paolo
 *
 */
public class ProvenanceBaseClient {

	PropertiesReader pr = null;

	String DB_URL_LOCAL = null;
	String DB_USER = null;
	String DB_PASSWD = null;

	ProvenanceAccess pAccess = null;
	public final ProvenanceAccess getpAccess() {
		return pAccess;
	}

	public final void setpAccess(ProvenanceAccess pAccess) {
		this.pAccess = pAccess;
	}

	ProvenanceQueryParser pqp = null;
	Query q = null;
	private String workflowAnnotationsFile;

	
	private InvocationContext ic = null;
	boolean derefValues = false;

	private static Logger logger = Logger.getLogger(ProvenanceBaseClient.class);

	public ProvenanceBaseClient() {

		try {
			pr = new PropertiesReader();
			logger.info("using resource file "+pr.getBundleName());
			setDB();
		} catch (Exception e) {
			logger.fatal(e.getMessage()+" -- cannot continue");
			System.exit(1);
		}		

	}


	public ProvenanceBaseClient(String resourceFile) {

		try {
			pr = new PropertiesReader(resourceFile);
			logger.info("using resource file "+pr.getBundleName());
			setDB();
		} catch (Exception e) {
			logger.fatal(e.getMessage()+" -- cannot continue");
			System.exit(1);
		}
	}

	
	void setDB() {
		DB_URL_LOCAL = pr.getString("dbhost");  // URL of database server //$NON-NLS-1$
		DB_USER = pr.getString("dbuser");                        // database user id //$NON-NLS-1$
		DB_PASSWD = pr.getString("dbpassword"); //$NON-NLS-1$
	}
	
		
	/**
	 * default connector type is Derby
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		setUp(ProvenanceConnectorType.DERBY);   
	}


	/**
	 * use explicit connector type
	 * @param db
	 * @throws Exception
	 */
	protected void setUp(String connectorType) throws Exception {

		setDataSource(connectorType);
		System.setProperty("raven.eclipse","true");

		pAccess = new ProvenanceAccess(connectorType);  // creates and initializes the provenance API

		pqp = new ProvenanceQueryParser();  // also creates QueryParser for clients that need to process XML queries

		configureInterface();              // sets user-defined preferences

		if (derefValues) setInvocationContext(pAccess.getInvocationContext());	
	}



	protected  void setDataSource(String connectorType) {

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.osjava.sj.memory.MemoryContextFactory");
		System.setProperty("org.osjava.sj.jndi.shared", "true");

		BasicDataSource ds = new BasicDataSource();
		ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		ds.setMaxActive(50);
		ds.setMinIdle(10);
		ds.setMaxIdle(50);
		ds.setDefaultAutoCommit(true);

		if (connectorType.equals(ProvenanceConnectorType.MYSQL)) {
			ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUsername(DB_USER);
			ds.setPassword(DB_PASSWD);

			try {
				ds.setUrl("jdbc:mysql://"+DB_URL_LOCAL+"/T2Provenance");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}  else if (connectorType.equals(ProvenanceConnectorType.DERBY)) {
			ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
			System.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");

			try {
				//
				//				ds.setUrl("jdbc:derby://localhost/T2Provenance");
				ds.setUrl("jdbc:derby:t2-database;create=true;upgrade=true");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		InitialContext context;
		try {
			context = new InitialContext();
			context.rebind("jdbc/taverna", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	/**
	 * set user-defined values for toggles on the API
	 */
	protected void configureInterface() {

		// do we need to return output processor values in addition to inputs?
		String returnOutputsPref = pr.getString("query.returnOutputs");
		if (returnOutputsPref != null) {
			pAccess.toggleIncludeProcessorOutputs(Boolean.parseBoolean(returnOutputsPref));
			logger.info("query returns output values in addition to inputs: "+pAccess.isIncludeProcessorOutputs());
		}

		// do we need to record actual values as part of the OPM graph?
		String recordArtifacValuesPref = pr.getString("OPM.recordArtifactValues");
		if (recordArtifacValuesPref != null) {			
			pAccess.toggleAttachOPMArtifactValues(Boolean.parseBoolean(recordArtifacValuesPref));
			logger.info("OPM.recordArtifactValues: "+ pAccess.isAttachOPMArtifactValues());
		}


		String computeOPMGraph = pr.getString("OPM.computeGraph");
		if (computeOPMGraph != null) {
			pAccess.toggleOPMGeneration(Boolean.parseBoolean(computeOPMGraph));
			logger.info("OPM.computeGraph: "+pAccess.isOPMGenerationActive());			
		}


		// NOTE this is a client feature: the API only returns references. They are deref'd locally
		String derefValuesString = pr.getString("query.returnDataValues");
		if (derefValuesString != null) {
			logger.info("query.returnDataValues: "+derefValuesString);
			derefValues = Boolean.parseBoolean(derefValuesString);
		}
		
		
		// annotations file used to answer semantic queries
		String workflowAnnotationsFile = pr.getString("workflow.annotations");
		if (workflowAnnotationsFile != null) {
			activateSemanticAnnotations(workflowAnnotationsFile);
			logger.info("workflow.annotations: "+getWorkflowAnnotationsFile());			
		}  else {
			logger.info(" no workflowAnnotationsFile specified -- semantic queries will not be supported");
		}
		
	}



	/**
	 * Parse the reference contained in the string and return a
	 * {@link T2Reference} with the correct properties
	 */
	public T2Reference referenceFromString(String reference) {
		T2ReferenceImpl newRef = new T2ReferenceImpl();
		Map<String, String> parseRef = parseRef(reference);
		newRef.setNamespacePart(parseRef.get("namespace"));
		newRef.setLocalPart(parseRef.get("localPart"));
		String type = parseRef.get("type");
		if (type.equals("ref")) {
			newRef.setReferenceType(T2ReferenceType.ReferenceSet);
		} else if (type.equals("list")) {
			newRef.setReferenceType(T2ReferenceType.IdentifiedList);
			newRef.setContainsErrors(Boolean
					.parseBoolean(parseRef.get("error")));
			newRef.setDepth(Integer.parseInt(parseRef.get("depth")));
		} else if (type.equals("error")) {
			newRef.setContainsErrors(true);
			newRef.setReferenceType(T2ReferenceType.ErrorDocument);
			newRef.setDepth(Integer.parseInt(parseRef.get("depth")));
		} else {
			return null;
			// should throw an error
		}

		return newRef;
	}


	/**
	 * Parse the reference and return a map with localPart, namespace, depth,
	 * contains errors and the type
	 * 
	 * @param ref
	 * @return
	 */
	private Map<String, String> parseRef(String ref) {
		String[] split = ref.split("\\?");
		// get the bit before and after the final '/' ie. the local part and the
		// depth, there might not be a split1[1] since it might not be a list
		String[] split2 = split[1].split("/");
		// get the t2:abc:// and the namespace
		String[] split3 = split[0].split("//");
		// get the t2 bit and the reference type bit
		String[] split4 = split3[0].split(":");

		Map<String, String> refPartsMap = new HashMap<String, String>();
		refPartsMap.put("type", split4[1]);
		refPartsMap.put("namespace", split3[1]);
		refPartsMap.put("localPart", split2[0]);

		if (refPartsMap.get("type").equals("list")) {
			refPartsMap.put("error", split2[1]);
			refPartsMap.put("depth", split2[2]);
		}
		if (refPartsMap.get("type").equals("error")) {
			refPartsMap.put("depth", split2[1]);
		}

		return refPartsMap;

	}       



	Object dereference(String stringRef) {

		T2Reference ref = referenceFromString(stringRef);

		if ( ref != null ) {
			return getInvocationContext().getReferenceService().resolveIdentifier(ref, null, getInvocationContext());		
		}
		return null;
	}



	public void activateSemanticAnnotations(String workflowAnnotationsFile) { 
		this.workflowAnnotationsFile = workflowAnnotationsFile; 
		getPqp().setWorkflowAnnotationsFile(workflowAnnotationsFile);
	}

	public String getWorkflowAnnotationsFile() { return workflowAnnotationsFile; }

	
	/**
	 * @return the pqp
	 */
	public ProvenanceQueryParser getPqp() { return pqp; }

	/**
	 * @param pqp the pqp to set
	 */
	public void setPqp(ProvenanceQueryParser pqp) { this.pqp = pqp; }

	public void setInvocationContext(InvocationContext ic) {
		this.ic = ic;
	}

	public InvocationContext getInvocationContext() {
		return ic;
	}

	
}
