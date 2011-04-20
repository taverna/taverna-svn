/**
 * 
 */
package net.sf.taverna.t2.provenance.client.XMLQuery;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.provenance.client.Janus.JanusOntology;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author paolo
 * takes a semantic condition from a Where clause in the query and evaluates it against the annotations file.
 *
 */
public class SemanticConditionEvaluator {

	Model      m  = null;

	private String annotationFile;

	private static Logger logger = Logger.getLogger(SemanticConditionEvaluator.class);
	
	private static final String PROVENIR_PREFIX = "knoesis";
	private static final String PROVENIR_NS = "http://knoesis.wright.edu/provenir/provenir.owl#";
	private static final String OBO_NS = "http://obofoundry.org/ro/ro.owl#";
	private static final String OBO_PREFIX = "obo";
	private static final String RDFS_PREFIX = "rdfs";
	private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String JANUS_NS = JanusOntology.getURI();
	private static final String JANUS_PREFIX = "janus";
	private static final String MYGRID_NS = "http://www.mygrid.org.uk/ontology#";
	private static final String MYGRID_PREFIX = "mygrid";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String RDF_PREFIX = "rdf";	
	
	private static Property RDFS_COMMENT = null;
	
	public SemanticConditionEvaluator(String workflowAnnotationsFile) { 
		this.annotationFile = workflowAnnotationsFile;
		init();
		logger.info("semantic condition evaluator created with annotations file: "+workflowAnnotationsFile); 
		}

	public String getAnnotationFile() { return annotationFile; }

	public void setAnnotationsFile(String workflowAnnotationsFile) {  this.annotationFile = annotationFile; }

	public void init() {

		// load the annotations file into a Jena model
		try {
			m = openModel(getAnnotationFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (m == null) return;
		
		m.setNsPrefix("janus",JanusOntology.getURI());
		m.setNsPrefix(PROVENIR_PREFIX, PROVENIR_NS);
		m.setNsPrefix(OBO_PREFIX, OBO_NS);
		m.setNsPrefix(RDFS_PREFIX, RDFS_NS);
		m.setNsPrefix(JANUS_PREFIX, JANUS_NS);
		m.setNsPrefix(MYGRID_PREFIX, MYGRID_NS);
		m.setNsPrefix(RDF_PREFIX, RDF_NS);
		
		RDFS_COMMENT = m.createProperty(RDFS_NS, "comment");
	}
	
	
	
	/**
	 * 
	 * @param workflowElementType
	 * @param whereClause
	 * @return a set of workflow elements that satisfy the condition, <br/>
	 * or: null if the condition cannot be evaluated and it should ignored, </br>
	 * the empty list denotes that the condition is valid but evaluates to the empty set, so no port is selected
	 */
	public List<String> evaluateCondition(String workflowElementType, String whereClause) {
		
		List<String> goodElements = new ArrayList<String>();
		String typematch = null;
		
		// expect to parse the whereClause as <property> <object>
		String[] clauseElements = parseWhereClause(whereClause);
		
		if (clauseElements == null) return null;  // condition cannot be evaluated
		
		// generate and execute simple SPARQL
		if (workflowElementType.equals(ProvenanceQueryParser.SELECTION_WORKFLOW_EL)) {
			typematch = "?element rdf:type janus:workflow_spec";
		}  else 		if (workflowElementType.equals(ProvenanceQueryParser.SELECTION_PROCESSOR_EL)) {
			typematch = "?element rdf:type janus:processor_spec";
		} else if (workflowElementType.equals(ProvenanceQueryParser.SELECTION_PORT_EL)) {
			typematch = "?element rdf:type janus:port";
		}
		
		String whereQuery = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n " +
			"PREFIX mygrid: <http://www.mygrid.org.uk/ontology#> \n" +
			"PREFIX janus: <http://purl.org/taverna/janus#> \n"+
			"SELECT ?element \n WHERE { ?element "+clauseElements[0]+" "+clauseElements[1]+" \n"+
			"OPTIONAL {"+typematch+"}}";
				
		logger.debug("mapping condition "+whereClause+" at the "+workflowElementType+" level into query: \n"+whereQuery);
		
		logger.debug("results:");
		ResultSet results = execSPRQL(whereQuery);
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Resource result = ((Resource) sol.get("element"));
			goodElements.add(result.getLocalName());
			logger.debug(result.getLocalName());
		}
		logger.debug("end of results");
		return goodElements;
	}
	
	
	/**
	 * this initial version supports a very simple form of where clause
	 * @param whereClause a clause of the form <pre> <property> <object></pre>
	 * @return the two parts of the clause, or null if invalid syntax
	 */
	private String[] parseWhereClause(String whereClause) {
		
		String[] elements = whereClause.split("[ ]+");
		if (elements.length != 2) {
			logger.warn("invalid syntax for where clause: "+whereClause);
			return null;
		}
		return elements;
	}

	private Model openModel(String modelName) throws FileNotFoundException {

		m = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
//		InputStream in = new FileReader FileManager.get().open( modelName );
		FileReader in;
			in = new FileReader ( modelName );
//		if (in == null) {
//			throw new java.io.FileNotFoundException();
//		}

		// read the RDF/XML file
		m.read(in, null);
		return m;		
	}

	
	
	public  ResultSet execSPRQL(String qstring) {
		return execSPRQL(qstring, getModel());
	}
	
	/**
	 * util: execute a SPRQL query
	 * @param qstring a valid SPRQL query string
	 * @return a Jena ResultSet
	 */
	public  ResultSet execSPRQL(String qstring, Model m ) {
		
		if (m == null)  {
			logger.fatal("null model for query");
			return null;  // should raise an exception
		}
				
		//logger.debug("QUERY: ["+qstring+"]");
		
		Query q = QueryFactory.create(qstring);
		QueryExecution qexec = QueryExecutionFactory.create(q, m);
		return qexec.execSelect();
	}
	
	
	/**
	 * @return the m
	 */
	public Model getModel() { return m; }

	/**
	 * @param m the m to set
	 */
	public void setModel(Model m) { this.m = m; }

	
	
}
