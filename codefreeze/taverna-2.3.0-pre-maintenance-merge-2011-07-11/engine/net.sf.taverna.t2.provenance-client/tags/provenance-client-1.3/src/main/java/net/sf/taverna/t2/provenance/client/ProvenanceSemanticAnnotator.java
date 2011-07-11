/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.client.Janus.JanusOntology;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author paolo
 * 
 * this util propagates workflow-level semantic annotations to traces for workflow runs. <br/>
 * It takes a workflow annotation file with lines of the form<br/>
 * <workflow_element> <property> <resource> <br/>
 * and produces an RDF graph where values and processor instances are annotated. The resulting graph is saved in RDF/XML format <p/>
 * It also produces a workflow-level graph for use by the provenance query preprocessor
 * Usage: -Dconf=<config file> -
 */
public class ProvenanceSemanticAnnotator extends ProvenanceBaseClient {

	private static final Object PROCESSOR_TYPE = "processor";
	private static final Object PORT_TYPE = "port";
	private static final String TRACE_ANNOTATIONS = "src/main/resources/traceAnnotations";
	
	private static Logger logger = Logger.getLogger(ProvenanceSemanticAnnotator.class);
	
	private static final String DEF_MODEL_NAME = "janus-instance-graph.rdf";
	private static final String BASE_DIR = "src/main/resources/";
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

	ModelMaker mm = null;
	Model      m  = null;

	String modelName = DEF_MODEL_NAME;
	
	private String runID = null;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String workflowAnnotations = null;
		String runID = null;

		ProvenanceSemanticAnnotator psm = null;
		if (System.getProperty("conf") != null) {
			psm = new ProvenanceSemanticAnnotator(System.getProperty("conf") );
		} else {
			psm = new ProvenanceSemanticAnnotator();
		}
		
		if (args.length > 0)  {  // first parm is workflow annotation file
			workflowAnnotations = args[0];
			logger.info("annotating trace from workflow annotations "+workflowAnnotations);
		}  else {
			System.out.println("Usage: ProvenanceSemanticAnnotator [-Dconf=<config file>] <workflowAnnotationsFile> [<runID>]");
			System.exit(0);
		}
		
		if (args.length > 1)  { // second optional arg is runID
			runID = args[1];
			logger.info("annotating runID "+runID);
		} else {
			runID = psm.getPAccess().getLatestRunID();
			logger.info("annotating latest run: "+runID);
		}
		psm.setUp();
		
		String traceAnnotations = psm.generateTraceAnnotations(workflowAnnotations, runID);
		
		
		String targetFile = TRACE_ANNOTATIONS+"-"+runID+".rdf";
		FileWriter fw = new FileWriter(new File(targetFile));
		fw.write(traceAnnotations);
		fw.close();		
		logger.info("annotations written to "+targetFile);
	}

	
	private ProvenanceAccess getPAccess() { return pAccess; }

	
	public ProvenanceSemanticAnnotator() throws Exception { super(); setUp(); init(); }

	public ProvenanceSemanticAnnotator(String resourceFile) throws Exception { super(resourceFile); setUp(); init(); }	
	
	
	void init() {
		
		m = ModelFactory.createDefaultModel();
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
	 * @param workflowAnnotationsFile
	 * @param runID  if null, latest available run is used
	 * @return an RDF/XML file with instance-level annotations
	 * @throws IOException 
	 */	
	public String generateTraceAnnotations(String workflowAnnotationsFile, String runID) throws IOException
	{
		
		String line = null;
		InputStreamReader isr = new InputStreamReader(new FileInputStream(workflowAnnotationsFile));
		BufferedReader br= new BufferedReader(isr);
		while ((line = br.readLine()) != null) {
			
			String[] splitLine = line.split("[ ]+");
			
			if (splitLine.length != 3) { 
				logger.warn("syntax error in workflow annotations file");
				return null;
			}
			String[] typedElement = splitLine[0].split(":");
			if (typedElement.length != 2)  { 
				logger.warn("syntax error in workflow annotations file");
				return null;
			}
			String element = typedElement[0];
			String type    = typedElement[1];
			
			if (type.equals(PROCESSOR_TYPE))  {
				//annotateProcessorBinding(element, splitLine[1], splitLine[2], runID);				
			} else if (type.equals(PORT_TYPE)) {
				annotateVarBinding(element, splitLine[1], splitLine[2], runID);
			}
		}
			
		return modelToString();
	}



	/**
	 * 
	 * @param element expect format <processor>/<port>
	 * @param property
	 * @param runID 
	 * @param object
	 */
	private void annotateVarBinding(String element, String property, String object, String runID) {
		
		String[] elements = element.split("/");
		if (elements.length != 2) {
			logger.fatal(elements+" wrong format");
			return;
		}
		
		String pname = elements[0];
		String vname = elements[1];
		
		// find Varbindings for the mentioned workflow element
		Map<String, String> vbConstraints = new HashMap<String, String>();
		vbConstraints.put("VB.processorNameRef", pname);
		vbConstraints.put("VB.portName", vname);
		vbConstraints.put("VB.workflowRunId", runID);
		
		List<PortBinding> vbs = null;
		try {
			vbs = getPAccess().getPortBindings(vbConstraints);
		} catch (SQLException e) {
			logger.fatal("SQL access troubles.");
			return;
		}
		
		// for each of them, create a new triple with <value>, <property> <object>
		logger.info(vbs.size()+" varbindings to be annotated");
		for (PortBinding vb:vbs) {
			logger.debug("about to annotate vb "+vb.getValue()+" with property "+property+" and object "+object);
			
			// perform actual annotation
			Resource pResource = getModel().createResource(vb.getValue());
			Property p = m.createProperty(property);
			pResource.addProperty(p, m.createResource(object));
		}
	}


//	private void annotateProcessorBinding(String pname, String property, String object, String runID) {
//		
//		// find procBindings for the mentioned workflow element
//		Map<String, String> pbConstraints = new HashMap<String, String>();
//		pbConstraints.put("PB.processorNameRef", pname);
//		pbConstraints.put("PB.workflowRunId", runID);
//		
//		List<ProcessorBinding> pbs = null;
//		try {
//			pbs = getPAccess().getProcessorBindings(pbConstraints);
//		} catch (SQLException e) {
//			logger.fatal("SQL access troubles.");
//			return;
//		}
//		
//		// for each of them, create a new triple with <value>, <property> <object>
//		logger.info(pbs.size()+" procbindings to be annotated");
//		for (ProcessorBinding pb:pbs) {
//			logger.debug("about to annotate pb "+pb.getPNameRef()+" with property "+property+" and object "+object);
//			
//			// perform actual annotation
//			Resource pResource = getModel().createResource(makeURI(pb.getPNameRef(), runID));
//			Property p = m.createProperty(property);
//			pResource.addProperty(p, m.createResource(object));
//		}
//	}


// read in the workflow annotations file

// for each triple in the file, apply the annotation propagation rule as follows:

// locate instance-level trace records that are relevant for the workflow element, and apply the appropriate rule

// port annotation --> VarBinding annotation

// processor annotation --> ProcBinding annotation

	private String modelToString() {
		
		StringWriter sw = new StringWriter();
		m.write(sw);
		return sw.toString();
	}

	
	public Model getModel() { return m; }
	
	private String getRunID() { return runID; }

}
