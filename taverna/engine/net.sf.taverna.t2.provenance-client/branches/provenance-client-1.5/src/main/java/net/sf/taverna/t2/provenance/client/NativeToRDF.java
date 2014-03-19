/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.provenance.client.Janus.StandAloneRDFProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.Collection;
import net.sf.taverna.t2.provenance.lineageservice.utils.DataLink;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Workflow;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

/**
 * @author paolo
 *
 */
public class NativeToRDF extends ProvenanceBaseClient {

	static Logger logger = Logger.getLogger(NativeToRDF.class);
	private static URL resource;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		NativeToRDF n2RDF = null;
		if (System.getProperty("conf") != null) {
			n2RDF = new NativeToRDF(System.getProperty("conf") );
		} else {
			n2RDF = new NativeToRDF();
		}
		n2RDF.setUp();
		n2RDF.generateRDF();
	}

	public NativeToRDF() { super(); }

	public NativeToRDF(String resourceFile) { super(resourceFile); }

	private String topLevelWorkflowID;

	public void generateRDF() throws SQLException, FileNotFoundException {

		String latestRun = pAccess.getLatestRunID();		
		StandAloneRDFProvenanceWriter  RDFpw = new StandAloneRDFProvenanceWriter(pAccess.getPq());
		generateRDF(latestRun, RDFpw);
	}

	public void generateRDF(String runId, StandAloneRDFProvenanceWriter RDFpw)
			throws SQLException {
		logger.info("generating RDF for run ID: "+runId);

		List<Workflow> latestWorkflows = pAccess.getWorkflowsForRun(runId);

		topLevelWorkflowID = pAccess.getTopLevelWorkflowID(runId);

		for (Workflow w:latestWorkflows) {
			// add each wfID to the RDF graph

			// single out the top level workflow in the list
			RDFpw.addWFId(w.getWorkflowId(), w.getParentWorkflowId(), w.getExternalName(), null);

			// also add the <workflow ID, wfinstanceID> pair
			RDFpw.addWorkflowRun(w.getWorkflowId(), runId);
		



		// add all processors. 
		List<ProvenanceProcessor> allProcessors = pAccess.getProcessorsForWorkflowID(w.getWorkflowId());

		for (ProvenanceProcessor pp:allProcessors) {
			boolean isTopLevel = (pp.getFirstActivityClassName().equals("net.sf.taverna.t2.activities.dataflow.DataflowActivity") &&
					pp.getWorkflowId().equals(topLevelWorkflowID) ? true: false);
			RDFpw.addProcessor(pp.getProcessorName(), pp.getFirstActivityClassName(), pp.getWorkflowId(), isTopLevel);
		}

		// add all Ports		
		List<Port> allPorts = pAccess.getAllPortsInDataflow(w.getWorkflowId());

		for (Port v:allPorts) { RDFpw.addPort(v); }

		// add all DataLinks
		HashMap<String, String> queryConstraints = new HashMap<String, String>();
		queryConstraints.put("instanceID", runId);
		
		List<DataLink> arcs = pAccess.getDataLinks(w.getWorkflowId());

		for (DataLink a:arcs) {
			RDFpw.addDataLink(a.getSourcePortName(), a.getSourceProcessorName(), 
					a.getDestinationPortName(), a.getDestinationProcessorName(), a.getWorkflowId());
		}
		}


		// add all processorBindings 
		//		for (Workflow w: latestWorkflows) {
		//queryConstraints.clear();
		//queryConstraints.put("workflowRunId", latestRun);
		//List<ProcessorBinding> bindings = pAccess.getProcBindings(queryConstraints);
		//for (ProcessorBinding pb: bindings) { RDFpw.addProcessorBinding(pb); }
		//		}


		// add all collections CHECK this appears to be incomplete!!
		List<Collection> collections = pAccess.getCollectionsForRun(runId);
		for (Collection c:collections) { RDFpw.addCollection(c.getCollId()); }

		// add all varBindings
		HashMap<String, String> queryConstraints = new HashMap<String, String>();
		queryConstraints.put("VB.workflowRunId", runId);
		List<PortBinding> varBindings = pAccess.getPortBindings(queryConstraints);

		for (PortBinding vb:varBindings) { 
			T2Reference ref = getInvocationContext().getReferenceService().referenceFromString(vb.getValue());			

			// Identified data = ic.getReferenceService().resolveIdentifier(ref, null, ic);

			Object data = getInvocationContext().getReferenceService().renderIdentifier(ref, Object.class, getInvocationContext()); 

			//			logger.info("data for ref "+vb.getValue()+" : "+ data);

			RDFpw.addPortBinding(vb, data);
		}
		RDFpw.closeCurrentModel();
	}



}
