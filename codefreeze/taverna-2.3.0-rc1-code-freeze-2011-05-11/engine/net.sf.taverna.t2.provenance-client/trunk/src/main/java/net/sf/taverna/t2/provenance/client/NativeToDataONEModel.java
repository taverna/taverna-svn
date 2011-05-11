/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.QueryPort;
import net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowTree;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Paolo Missier<p/>
 * This is a provenance API client designed to extract a complete set of dependencies from a native provenance DB 
 * so that it can be uploaded into the DataONE common provenance model<br/>
 * this class simplifies the users' task by using the hard-coded provenance query <it>completeGraph.xml</it> rather than reading 
 * a query file from an explicit user config.<br/>
 * All of the info extracted refers to the <it>latest run</it> stored in the DB. Therefore if you want to extract provenance
 * for a specific workflow, first run it in Taverna, then run this app.
 */
public class NativeToDataONEModel extends ProvenanceBaseClient {

	List<String> wfNames = null;
	Set<String> selectedProcessors = null;

	private static String queryFile = "src/main/resources/completeGraph.xml";

	private static Logger logger = Logger.getLogger(NativeToDataONEModel.class);
	
	Model m = null;

	public static void main(String[] args) throws Exception {

		Properties p = new Properties();

		p.put("query.file", queryFile);

		NativeToDataONEModel client = new NativeToDataONEModel();

		client.setUp();

		////// 
		// step 1: extract structural information from the DB
		//////
		client.reportStructure();

		////// 
		// step 2: extract dynamic provenance trace for the latest run, using a provenance query
		//////		
		QueryAnswer answer = client.queryProvenance(p);

		// look at this for clues on how to extract information from the native trace
		if (answer!=null) client.reportAnswer(answer);

		
		/// technical step:
		//  load OPM graph from answer into a Jena model
		client.setModel(client.loadOPMGraph(answer));
		
		
		////// 
		// step 4: upload data using references from the OPM graph.
		//         This generates a new graph with added assertions for the public data references
		//////
		client.setModel(client.publishDataAndMapReferences(client.getModel()));
		
		
		////// 
		// step 5: retrieve OPM relationships from the new OPM graph. Optionally report sameAs equivalences
		//////
		client.reportOPMRelations(client.getModel());

	}



	/**
	 * uses class OPMDAtaUploader, which can also be used as a standalone util that operates on OPM graph files in XML format
	 * @param model
	 * @return
	 * @throws IOException 
	 */
	private Model publishDataAndMapReferences(Model model) throws IOException {
		
		OPMDataUploader uploader = new OPMDataUploader();
		Model m  = null;
		
		FTPClient ftp = uploader.ftpConnect();

		if (ftp != null) { 
			 m = uploader.uploadAllData(model, ftp);		
		} else {
			logger.fatal("could not connect to ftp server, exiting");
		}
		uploader.ftpDisconnect(ftp);
		return m;
	}




	private Model loadOPMGraph(QueryAnswer answer) {

		String OPMGraph = answer.getOPMAnswer_AsRDF();
		
		// get the OPM graph, if available
		if (answer.getOPMAnswer_AsRDF() == null) {
			logger.info("save OPM graph: OPM graph was NOT generated.");
			return null;
		}
		return openModel(OPMGraph);		
	}



	/**
	 * reads in the OPM graph associated to the query answer in the RDF format and loads it into a Jena model so it can be queried easily 
	 * @param answer 
	 */
	private void reportOPMRelations(Model m) {

		logger.info("***  reporting OPM relations: ***");
		reportUsed();
		reportWasGeneratedBy();
	}



	private void reportUsed() {
		// query the model to extract the relations we need
		String usedQuery = 
			"PREFIX t: <http://taverna.opm.org/> \n"+
			"PREFIX opm: <http://www.ipaw.info/2007/opm#> \n"+			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
			"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
			"SELECT ?a ?p ?r ?a1\n"+
			"WHERE  { \n"+
			"?u rdf:type opm:Used .\n"+	
			"?u opm:usedArtifact ?a . \n"+
			"?u opm:usedByProcess ?p . \n"+
			"?u opm:usedRole  ?r . \n" +
			"OPTIONAL { ?a owl:sameAs ?a1 . }"+
			"}";

		ResultSet s = execSPRQL(usedQuery);

		if (!s.hasNext()) { logger.info("no Used resources found for query \n"+usedQuery); }

		while ( s.hasNext() ) {
			QuerySolution sol = s.nextSolution();

			String artifactID=null;
			String publicArtifactID=null;
			String processID =null;
			String roleID = null;
			
			Resource artifactResource =  sol.getResource("a"); 
			if (artifactResource!= null) artifactID = artifactResource.getURI();
			
			Resource publicArtifactResource =  sol.getResource("a1"); 
			if (publicArtifactResource!= null) {
				publicArtifactID = publicArtifactResource.getURI();
				logger.info("public reference: "+publicArtifactID+" maps to local reference: "+artifactID);
			}			
			
			Resource processResource =  sol.getResource("p"); 
			if (processResource!= null) processID = processResource.getURI();

			Resource roleResource =  sol.getResource("r"); 
			if (roleResource!= null) roleID = roleResource.getURI();

			logger.info("found Used("+artifactID+","+roleID+","+processID+")");
		}		
	}


	private void reportWasGeneratedBy() {

		// query the model to extract the relations we need
		String usedQuery = 
			"PREFIX t: <http://taverna.opm.org/> \n"+
			"PREFIX opm: <http://www.ipaw.info/2007/opm#> \n"+			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
			"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
			"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
			"SELECT ?a ?p ?r\n"+
			"WHERE  { \n"+
			"?u rdf:type opm:Generated .\n"+	
			"?u opm:generatedArtifact ?a . \n"+
			"?u opm:generatedByProcess ?p . \n"+
			"?u opm:generatedRole  ?r  \n"  +
			"OPTIONAL { ?a owl:sameAs ?a1 . }"+
			"}";

		ResultSet s = execSPRQL(usedQuery);

		if (!s.hasNext()) { logger.info("no WasGeneratedBy resources found for query \n"+usedQuery); }

		while ( s.hasNext() ) {
			QuerySolution sol = s.nextSolution();

			String artifactID=null;
			String publicArtifactID=null;
			String processID =null;
			String roleID = null;
			
			Resource artifactResource =  sol.getResource("a"); 
			if (artifactResource!= null) artifactID = artifactResource.getURI();
			
			Resource publicArtifactResource =  sol.getResource("a1"); 
			if (publicArtifactResource!= null) {
				publicArtifactID = publicArtifactResource.getURI();
				logger.info("public reference: "+publicArtifactID+" maps to local reference: "+artifactID);
			}			

			Resource processResource =  sol.getResource("p"); 
			if (processResource!= null) processID = processResource.getURI();

			Resource roleResource =  sol.getResource("r"); 
			if (roleResource!= null) roleID = roleResource.getURI();

			logger.info("found wasGeneratedBy("+artifactID+","+roleID+","+processID+")");
		}		
	
	}



	private void reportStructure() throws SQLException {

		// get the ID for the latest run in the DB
		String latestRunID = pAccess.getLatestRunID();
		
		String mainWorkflowUUID = pAccess.getTopLevelWorkflowID(latestRunID);
		
		WorkflowTree nestingStructure  = pAccess.getWorkflowNestingStructure(mainWorkflowUUID);
		
		logger.info("static workflow nestingStructure: ");
		logger.info(nestingStructure.toString());
		
		logger.info("extracting provenance for workflow: "+mainWorkflowUUID+ " and for run with ID: "+latestRunID);

		// ports for the entire workflow
		logger.info("here are the ports for the top level workflow: ");
		List<Port> ports = pAccess.getPortsForDataflow(mainWorkflowUUID);

		for (Port p:ports) {
			if (!p.isInputPort()) {
				logger.info("\tOUTPUT port "+p.getPortName());
			} else {
				logger.info("\tINPUT port "+p.getPortName());
			}
		}

		logger.info("listing all other processors (actors) along with their input and output ports (channels) for this workflow");

		Map<String, List<ProvenanceProcessor>> allProcessors = pAccess.getProcessorsInWorkflow(mainWorkflowUUID);

		List<ProvenanceProcessor> myProcs = allProcessors.get(mainWorkflowUUID);  // processors for this specific workflow
		for (ProvenanceProcessor pp:myProcs) {
			
			String pname = pp.getProcessorName();
			logger.info("processor: "+pname);
			
			logger.info("\twith ports: ");
			ports = pAccess.getPortsForProcessor(pp.getWorkflowId(), pname);

			for (Port p:ports) {
				if (!p.isInputPort()) {
					logger.info("\t\tOUTPUT port "+p.getPortName());
				} else {
					logger.info("\t\tINPUT port "+p.getPortName());
				}
			}
		}

		
	}



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

//		ProvenanceQueryParser pqp = new ProvenanceQueryParser();
//		pqp.setPAccess(pAccess);
//
//		q = pqp.parseProvenanceQuery(querySpecFile);
		q = getPqp().parseProvenanceQueryFile(querySpecFile);


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

	private void reportAnswer(QueryAnswer answer) {

		NativeAnswer nAnswer = answer.getNativeAnswer();

		// nAnswer contains a Map of the form 
		// 	Map<QueryVar, Map<String, List<Dependencies>>>  answer;

		logger.info("*** list of all provenance paths ***");
		
		Map<QueryPort, Map<String, List<Dependencies>>>  dependenciesByVar = nAnswer.getAnswer();	
		for (QueryPort v:dependenciesByVar.keySet()) {
			logger.info("reporting provenance paths for values on TARGET port: "+v.getProcessorName()+":"+v.getPortName()+":"+v.getPath());

			Map<String, List<Dependencies>> deps = dependenciesByVar.get(v);
			for (String path:deps.keySet()) {

				logger.info("provenance of value at position "+path);
				
				Map<String, String> constraints = new HashMap<String, String>();
				constraints.put("VB.portName", v.getPortName());
				constraints.put("VB.processorName", v.getProcessorName());
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
				Object value = getInvocationContext().getReferenceService().renderIdentifier(
						getInvocationContext().getReferenceService().referenceFromString(bindings.get(0).getValue()), Object.class, getInvocationContext());

				logger.info("\tvalue at position "+path+" is:\n\t "+value);
				
				// for each Dependencies in the list
				for (Dependencies dep:deps.get(path)) {

					// each list of records in Dependencies
					logger.info("\t\tdependencies at processor "+dep.getRecords().get(0).getProcessorName());
					for (LineageQueryResultRecord record: dep.getRecords()) {

						// we now resolve values on the client, there are no values in the record
						// returned through the API
						record.setPrintResolvedValue(false);  
						logger.info("\t\t\t"+"depends on: "+record.toString());

						// resolve reference if so desired
						if (derefValues && record.getValue() != null) {
							T2Reference ref = getInvocationContext().getReferenceService().referenceFromString(record.getValue());

							Object o = getInvocationContext().getReferenceService().renderIdentifier(ref, Object.class, getInvocationContext()); 
							logger.info("\t\t\tvalue: "+o);
						}
					}
				}
			}
		}		
	}

	
	//////////
	/// Jena-specific stuff
	//////////
	
	private Model getModel() { return m; }

	private void setModel(Model m) {
	       this.m = m;		
		}



	public  ResultSet execSPRQL(String qstring) {
		return execSPARQL(qstring, getModel());
	}


	/**
	 * util: execute a SPRQL query
	 * @param qstring a valid SPRQL query string
	 * @return a Jena ResultSet
	 */
	public  ResultSet execSPARQL(String qstring, Model m ) {

		if (m == null)  {
			logger.fatal("null model for query");
			return null;  // should raise an exception
		}

		logger.debug("QUERY: ["+qstring+"]");

		com.hp.hpl.jena.query.Query q = QueryFactory.create(qstring);
		QueryExecution qexec = QueryExecutionFactory.create(q, m);
		return qexec.execSelect();
	}


	private Model openModel(String OPMGraph) {

		Model m = ModelFactory.createDefaultModel();

			// read the RDF/XML string
		m.read(fromString(OPMGraph),null);
		return m;		
	}
	
	
	public static InputStream fromString(String str)
	{
	byte[] bytes = str.getBytes();
	return new ByteArrayInputStream(bytes);
	}
	


}
