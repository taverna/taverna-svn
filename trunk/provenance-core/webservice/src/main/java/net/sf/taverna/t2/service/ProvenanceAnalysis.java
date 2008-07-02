/**
 * 
 */
package net.sf.taverna.t2.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.service.util.Arc;
import net.sf.taverna.t2.service.util.ProcBinding;
import net.sf.taverna.t2.service.util.Var;
import net.sf.taverna.t2.service.util.VarBinding;

/**
 * @author paolo<p/>
 * the main class for querying the lineage DB
 * assumes a provenance DB ready to be queried
 */
public class ProvenanceAnalysis {

	ProvenanceQuery pq = null;
	
	public ProvenanceAnalysis() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		pq = new ProvenanceQuery();		
		
	}
	
	
	
	public List<String> getWFInstanceIDs() throws SQLException {
	
		return pq.getWFInstanceIDs();
		
	}
	
	
	/**
	 * lineage traversal algorithm. This is phase I of the query algorithm. 
	 * It takes one input variable (var,proc), whose lineage we want to compute, and an optional set of processors. 
	 * It traverses the static workflow graph and computes all paths between the input variable and 
	 * each of the processors<br/>
	 * Each path is annotated with signature mismatch indications. 
	 * These are used in phase II to compile the target queries
	 * (one for each path, unless we can prune/optimize)<p/>
	 * paths are generated one at a time, in a depth-first fashion
	 * @throws SQLException 
	 */
	public List<List<String>> computeLineagePaths(
								String workflowID,   // context
								String var,   // target var
								String proc,   // qualified with its processor name
								List<String> selectedProcessors  // processors where the paths end								
								) throws SQLException  {
		
		// get (var, proc) from Var
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", workflowID);
		varQueryConstraints.put("V.pnameRef", proc);  
		varQueryConstraints.put("V.varName", var);  
		
		List<Var> vars = pq.getVars(varQueryConstraints);
		
		if (vars.isEmpty())  {
			System.out.println("variable ("+var+","+proc+") not found, lineage query terminated");
			return null;
		}
		
		// expect one record
		Var v = vars.get(0);
		
		if (v.isInput()) {
			// if vName is input, then do a xfer() step

		} else {
			// if vName is output, then do a xform() step
			
		}
		return null;
		
	}
	

/*	
	public void getLineageTree(String wfInstanceID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		Map<String, String>  vbConstraints = new HashMap<String, String>();

		vbConstraints.put("V.wfInstanceRef", wfInstanceID);
		vbConstraints.put("V.PNameRef", "_OUTPUT_");  // this retrieves VarBindings for all output vars

		// get list of var bindings for all output vars
		List<VarBinding> vbList = pq.getVarBindings(vbConstraints);  

		System.out.println("step 0 in lineage trees: the outputs");

		for (VarBinding vb: vbList) {
			
			System.out.println(vb.toString());

			int iteration = vb.getIteration();

			System.out.println("retrieving arc with ["+vb.getPNameRef()+"/"+vb.getVarNameRef()+"] as sink");
			
			// xfer step: query the arcs to retrieve the output var for the next processor up
			Map<String, String>  arcConstraints = new HashMap<String, String>();

			arcConstraints.put("wfInstanceRef", wfInstanceID);
			arcConstraints.put("sinkPNameRef", vb.getPNameRef());
			arcConstraints.put("sinkVarNameRef", vb.getVarNameRef());
			
			List<Arc> arcs = pq.getArcs(arcConstraints);
			
			// we expect one single incoming arc into a var! this is a Taverna semantics constraint
			Arc incomingArc = arcs.get(0);  // NULL is an error -- disconnected dataflow??
			
			System.out.println("arc found. source: ["+incomingArc.getSourcePnameRef()+","+incomingArc.getSourceVarNameRef()+"]");
			System.out.println("retrieving procBinding for processor ["+incomingArc.getSourcePnameRef()+"]");

			// query the ProcBinding to get the bindings for the processor involved in the xform step
			Map<String, String>  procConstraints = new HashMap<String, String>();
			
			procConstraints.put("execIDRef", wfInstanceID);
			procConstraints.put("pnameRef", incomingArc.getSourcePnameRef());
			// add iteration constraint from the VarBinding
			procConstraints.put("iteration", Integer.toString(iteration));
			
			List<ProcBinding> procBindings = pq.getProcBindings(procConstraints);
			
			// expect exactly one proc
			// report the binding as part of the lineage tree
			ProcBinding pb = procBindings.get(0);
			
			System.out.println(pb.toString());			
			
			// get the VarBindings for each of xformingProc's input vars
			vbConstraints.clear();
			
			vbConstraints.put("V.wfInstanceRef", wfInstanceID);
			vbConstraints.put("V.PNameRef", incomingArc.getSourcePnameRef());
			vbConstraints.put("V.inputOrOutput", "1");
			vbConstraints.put("VB.iteration", Integer.toString(iteration));
			
			vbList = pq.getVarBindings(vbConstraints);  

			System.out.println("step 1 in lineage tree");

			for (VarBinding vb1: vbList) {
				
				System.out.println("var binding: \n"+ vb1.toString());
			
			}
			
			// repeat
			
		}  // end for each vb:vbList		
		
	}  // end getLineageTrees
	*/
	
}
