/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.taverna.t2.provenance.api.NativeAnswer;
import net.sf.taverna.t2.provenance.api.Query;
import net.sf.taverna.t2.provenance.api.QueryAnswer;
import net.sf.taverna.t2.provenance.client.XMLQuery.QueryParseException;
import net.sf.taverna.t2.provenance.client.XMLQuery.QueryValidationException;
import net.sf.taverna.t2.provenance.lineageservice.Dependencies;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.QueryPort;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

/**
 * @author Paolo Missier<p/>
 * Example provenance API client.  <br/>
 * this class reads config file APIClient.properties from folder api.client.resources to find the filename of a query to execute,
 * for instance query.file=src/main/resources/minimal.xml
 *   
 */
public class ProvenanceAPISampleClient extends ProvenanceBaseClient {

	private static final String DEFAULT_OPM_FILENAME = "src/test/resources/OPMGraph.rdf";

	static String OPMGraphFilename = null;

	List<String> wfNames = null;
	Set<String> selectedProcessors = null;

	private static Logger logger = Logger.getLogger(ProvenanceAPISampleClient.class);

	/**
	 * Creates an instance of the client, uses it to submit a pre-defined query, and displays the results on a console 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		ProvenanceAPISampleClient client = null;
		if (System.getProperty("conf") != null) {
			client = new ProvenanceAPISampleClient(System.getProperty("conf") );
		} else {
			client = new ProvenanceAPISampleClient();
		}

		Properties p = null;  // not used

		client.setUp();
		OPMGraphFilename = client.setOPMFilename();

		QueryAnswer answer = client.queryProvenance(p);

		client.reportAnswer(answer);
		client.saveOPMGraph(answer, OPMGraphFilename);
	}

	public ProvenanceAPISampleClient() { super(); }

	public ProvenanceAPISampleClient(String resourceFile) { super(resourceFile); }


	protected  QueryAnswer queryProvenance() throws QueryParseException, QueryValidationException {
		return queryProvenance(null);
	}


	/**
	 * parses an XML provenance query into a Query object and invokes {@link ProvenanceAccess.executeQuery()} 
	 * @return a bean representing the query answer
	 * @throws QueryValidationException 
	 * @throws QueryParseException 
	 * @see QueryAnswer
	 */
	protected  QueryAnswer queryProvenance(Properties p) throws QueryParseException, QueryValidationException {

		Query q = new Query();

		String querySpecFile = null;
		if (p != null && p.get("query.file") != null)  
			querySpecFile = (String) p.get("query.file");
		else {
			// get filename for XML query spec
			querySpecFile = pr.getString("query.file");
		}
		logger.info("executing query "+querySpecFile);

		// pm added 9/10
		// optionally set the workflow annotations file here to support semantic queries
		
		q = getPqp().parseProvenanceQuery(querySpecFile);

		if (q == null) {
			logger.fatal("query processing failed. So sorry.");
			return null;
		}
		logger.info("YOUR QUERY: "+q.toString());

		QueryAnswer answer=null;
		try {
			answer = pAccess.executeQuery (q);
		} catch (SQLException e) {
			logger.fatal("Exception while executing query: "+e.getMessage());
			return null;
		}
		return answer;
	}



	/////////
	/// preliminary setup methods
	/////////

	// user-selected file name for OPM graph?
	protected String setOPMFilename() {

		String OPMGraphFilename = pr.getString("OPM.rdf.file");
		if (OPMGraphFilename == null) {
			OPMGraphFilename = DEFAULT_OPM_FILENAME;
			logger.info("OPM.filename: "+OPMGraphFilename);			
		}
		return OPMGraphFilename;
	}



	/**
	 * writes the RDF/XML OPM string to file
	 * @param opmFilename
	 */
	private void saveOPMGraph(QueryAnswer answer, String opmFilename) {

		if (answer.getOPMAnswer_AsRDF() == null) {
			logger.info("save OPM graph: OPM graph was NOT generated.");
			return;
		}

		try {
			FileWriter fw= new FileWriter(new File(opmFilename));
			fw.write(answer.getOPMAnswer_AsRDF());
			fw.close();
		} catch (IOException e) {
			logger.warn("saveOPMGraph: error saving graph to file "+opmFilename);
			logger.warn(e.getMessage());
		}
		logger.info("OPM graph saved to "+opmFilename);
	}



	private void reportAnswer(QueryAnswer answer) {

		NativeAnswer nAnswer = answer.getNativeAnswer();

		// nAnswer contains a Map of the form 
		// 	Map<QueryPort, Map<String, List<Dependencies>>>  answer;

		System.out.println("*** native answer to the query ***");

		Map<QueryPort, Map<String, List<Dependencies>>>  dependenciesByVar = nAnswer.getAnswer();	
		for (QueryPort v:dependenciesByVar.keySet()) {
			System.out.println("reporting dependencies for values on TARGET port: "+v.getProcessorName()+":"+v.getPortName()+":"+v.getPath());

			Map<String, List<Dependencies>> deps = dependenciesByVar.get(v);
			for (String path:deps.keySet()) {

				Map<String, String> constraints = new HashMap<String, String>();
				constraints.put("VB.portName", v.getPortName());
				constraints.put("VB.processorNameRef", v.getProcessorName());
				constraints.put("VB.iteration", path);
				constraints.put("VB.workflowId", v.getWorkflowId());
				constraints.put("VB.workflowRunId", v.getWorkflowRunId());

				List<PortBinding> bindings = null;
				try {
					bindings = pAccess.getPortBindings(constraints);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Object value = ic.getReferenceService().renderIdentifier(
						ic.getReferenceService().referenceFromString(bindings.get(0).getValue()), Object.class, ic);

				System.out.println("\tdependencies for value:\n\t "+value+"\n\t bound to path "+path);
				for (Dependencies dep:deps.get(path)) {

					for (LineageQueryResultRecord record: dep.getRecords()) {

						// we now resolve values on the client, there are no values in the record
						// returned through the API
						record.setPrintResolvedValue(false);  
						System.out.println("\t\t"+record.toString());

						// resolve reference if so desired
						if (derefValues && record.getValue() != null) {
							T2Reference ref = ic.getReferenceService().referenceFromString(record.getValue());

							Object o = ic.getReferenceService().renderIdentifier(ref, Object.class, ic); 
							System.out.println("\t\tvalue: "+o);
						}
					}
				}
			}
		}		
	}


}
