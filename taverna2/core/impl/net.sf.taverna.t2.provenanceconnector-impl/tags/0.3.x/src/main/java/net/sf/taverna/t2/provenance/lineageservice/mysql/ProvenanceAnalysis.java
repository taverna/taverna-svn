/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.provenance.lineageservice.AnnotationsLoader;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.utils.Arc;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;

/**
 * @author paolo<p/>
 * the main class for querying the lineage DB
 * assumes a provenance DB ready to be queried
 */
public class ProvenanceAnalysis {

	private static final String IP_ANNOTATION = "index-preserving";

	ProvenanceQuery pq = null;
	AnnotationsLoader al = null;

	Map<String,List<String>> annotations = null;  // user-made annotations to processors

	public ProvenanceAnalysis() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		pq = new MySQLProvenanceQuery();		

		al = new AnnotationsLoader();  // singleton

	}


	public void setAnnotationFile(String annotationFile) {

		annotations = al.getAnnotations(annotationFile);

		if (annotations == null) {
			System.out.println("WARNING: no annotations have been loaded");
			return;
		}

		System.out.println("processor annotations for lineage refinement: ");
		for (Map.Entry<String,List<String>> entry:annotations.entrySet())  {

			System.out.println("annotations for proc "+entry.getKey());
			for (String ann: (List<String>) entry.getValue()) {
				System.out.println(ann);
			}
		}

	}


	public List<String> getWFInstanceIDs() throws SQLException { return pq.getWFInstanceIDs(); }


	
	public LineageQueryResult fetchIntermediateResult(
			String wfInstance,
			String pname,
			String vname,
			String iteration) throws SQLException  {
		
		LineageSQLQuery lq = pq.simpleLineageQuery(wfInstance, pname, vname, iteration);
		
		return pq.runLineageQuery(lq);
		
	}
	
//	runLineageQuery
	
	
	/**
	 * compute lineage queries using path projections
	 * @param wfInstance the (single) instance defines the scope of a query
	 * @param var
	 * @param proc
	 * @param path  within var (can be null)
	 * @param selectedProcessors  only report lineage when you reach any of these processors
	 * @throws SQLException
	 */
	public void clp3(
			String wfInstance,   // context
			String var,   // target var
			String proc,   // qualified with its processor name
			String path, 
			Set<String> selectedProcessors
	) throws SQLException  {


		List<LineageSQLQuery>  lqList = new ArrayList<LineageSQLQuery>();  // holds the output

		// start with xfer or xform depending on whether initial var is output or input

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", wfInstance);
		varQueryConstraints.put("V.pnameRef", proc);  
		varQueryConstraints.put("V.varName", var);  

		List<Var> vars = pq.getVars(varQueryConstraints);

		if (vars.isEmpty())  {
			System.out.println("variable ("+var+","+proc+") not found, lineage query terminated");
			return;
		}

		Var v = vars.get(0); 		// expect exactly one record

		if (v.isInput()) { // if vName is input, then do a xfer() step

			// rec. accumulates SQL queries into lqList
			xferStep2(wfInstance, var, proc, path, selectedProcessors, lqList);


		} else { // start with xform

			// rec. accumulates SQL queries into lqList
			xformStep(wfInstance, v, proc, path, selectedProcessors, lqList);

		}

		// execute queries in the LineageSQLQuery list
		pq.runLineageQueries(lqList);

	}  // end clp3

	
	/**
	 * 
	 * @param wfInstance
	 * @param var
	 * @param proc
	 * @param selectedProcessors
	 * @param path
	 * @param lqList
	 * @throws SQLException 
	 */
	private void xformStep(String wfInstance, 
			Var outputVar, // we need the dnl from this output var
 			String proc,
 			String path,
			Set<String> selectedProcessors, 
			List<LineageSQLQuery> lqList) throws SQLException {

		// retrieve input vars for current processor 
		Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

		varsQueryConstraints.put("wfInstanceRef", wfInstance);
		varsQueryConstraints.put("pnameRef", proc);  
		varsQueryConstraints.put("inputOrOutput", "1");  

		List<Var>  inputVars = pq.getVars(varsQueryConstraints);

		// processor may have no inputs.
		// this is not a prob when dping path projections
		// but we still want to generate SQL if this is a selected proc
		// in this case we flag proc as outputs-only and generate SQL for the current output
		// using the current path		
		
		///////////
		/// path projections
		///////////
		// maps each var to its projected path
		Map<Var,String> var2Path = new HashMap<Var,String>();
		Map<Var,Integer> var2delta = new HashMap<Var,Integer>();

		if (path == null) {  // nothing to split
			for (Var inputVar: inputVars)  var2Path.put(inputVar, null);
		} else {

			int minPathLength = 0;  // if input path is shorter than this we give up granularity altogether
			for (Var inputVar: inputVars) {
				int delta = inputVar.getActualNestingLevel() - inputVar.getTypeNestingLevel();
				var2delta.put(inputVar, new Integer(delta));
				minPathLength += delta;
			}

			String iterationVector[] = path.split(",");

			if (iterationVector.length < minPathLength) {  // no path is propagated
				for (Var inputVar: inputVars) {
					var2Path.put(inputVar, null);
				}
			} else { // compute projected paths

				String[] projectedPath; 

				int start = 0;
				for (Var inputVar: inputVars) {

					// 24/7/08 get DNL (declared nesting level) and ANL (actual nesting level) from VAR
					// TODO account for empty paths
					int projectedPathLength = var2delta.get(inputVar);  // this is delta			

					if (projectedPathLength > 0) {  // this var is involved in iteration

						projectedPath = new String[projectedPathLength];
						for (int i=0; i<projectedPathLength; i++) {					
							projectedPath[i] = iterationVector[start+i];
						}		
						start += projectedPathLength;

						StringBuffer iterationFragment = new StringBuffer();
						for (String s:projectedPath) { iterationFragment.append(s+","); }
						iterationFragment.deleteCharAt(iterationFragment.length()-1);

						var2Path.put(inputVar, iterationFragment.toString());
					} else {  // associate empty path to this var
						var2Path.put(inputVar, null);
					}
				}
			}
		}

		///////////
		/// generate SQL if necessary -- for all input vars, based on the current path
		/// the projected paths are required to determine the level in the collection at which 
		/// we look at the value assignment
		///////////
		if (selectedProcessors.isEmpty() || selectedProcessors.contains(proc)) {

			// dnl of output var defines length of suffix to path that we are going to use for query
			// if var2Path is null this generates a trivial query for the current output var and current path 
			LineageSQLQuery lq = pq.lineageQueryGen(wfInstance, proc, var2Path, outputVar, path);

			lqList.add(lq);

		}

		// recursion -- xfer path is next up

		for (Var inputVar: inputVars) {
			xferStep2(wfInstance, inputVar.getVName(), inputVar.getPName(), var2Path.get(inputVar), selectedProcessors, lqList);
		}

	}  // end xformStep



	private void xferStep2(String workflowID, 
			String var, 
			String proc,
			String path, 
			Set<String> selectedProcessors,
			List<LineageSQLQuery> lqList) throws SQLException {

		String sourceProcName = null;
		String sourceVarName  = null;

		// retrieve all Arcs ending with (var,proc) -- ideally there is exactly one
		// (because multiple incoming arcs are disallowed)
		Map<String, String>  arcsQueryConstraints = new HashMap<String, String>();

		arcsQueryConstraints.put("wfInstanceRef", workflowID);
		arcsQueryConstraints.put("sinkVarNameRef", var);  
		arcsQueryConstraints.put("sinkPNameRef", proc);  

		List<Arc> arcs = pq.getArcs(arcsQueryConstraints);

		Arc a = arcs.get(0);

		if (a != null) {
			// get source node
			sourceProcName = a.getSourcePnameRef();
			sourceVarName  = a.getSourceVarNameRef();
		}

		System.out.println("xfer() from ["+proc+","+var+"] to ["+sourceProcName+","+sourceVarName+"]");

		// CHECK transfer same path with only exception: when anl(sink) > anl(source)
		// in this case set path to null
		// retrieve full record for var:
		// retrieve input vars for current processor 
		Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

		varsQueryConstraints.put("wfInstanceRef", workflowID);
		varsQueryConstraints.put("pnameRef", sourceProcName);  
		varsQueryConstraints.put("varName", sourceVarName);  
		List<Var>  varList  = pq.getVars(varsQueryConstraints);
		
		Var outputVar = varList.get(0);

		// recurse on xform
		xformStep(workflowID, outputVar, sourceProcName, path, selectedProcessors, lqList);

	} // end xferStep2



/////////////////
//	OLD CLP BELOW
/////////////////







	/**
	 * this class represents the annotation (single or sequence, to be determined) 
	 * that are produced upon visiting the graph structure and that drive the generation of 
	 * a pinpoint lineage query<br/>
	 * this is still a placeholder
	 */
	class LineageAnnotation {

		List<String> path = new ArrayList<String>();

		boolean isXform = true;

		String iteration = "";  // this is the iteration projected on a single variable. Used for propagation upwards default is no iteration --
		String iterationVector = ""; // iteration vector accounts for cross-products. Used to be matched exactly in queries. 
		int iic = 0;  // index in collection -- default is 0 
		int collectionNesting = 0;  // n indicates granularity is n levels from leaf. 
		// This quantifies loss of lineage precision when working with collections
		String collectionRef = null;
		String proc;
		String var;
		String varType = null;   // dtring, XML,... see Taverna type system

		int DNL = 0; // declared nesting level -- copied from VAR
		int ANL  = 0;  // actual nesting level -- copied from Var

		String wfInstance;  // TODO generalize to list / time interval?

		public String toString() {

			StringBuffer sb = new StringBuffer();

			if (isXform)  sb.append(" xform: ");
			else sb.append(" xfer: ");

			sb.append("<PROC/VAR/VARTYPE, IT, IIC, ITVECTOR, COLLNESTING> = "+
					proc + "/" + var + "/" + varType +
					"," + "["+iteration +"]"+
					","+ iic + 
					", ["+ iterationVector + "]"+
					","+ collectionNesting);

			return sb.toString();
		}


		public void addStep(String step) {
			path.add(step);
		}

		public void removeLastStep() {
			path.remove(path.size()-1);
		}


		/**
		 * @return the path
		 */
		public List<String> getPath() {
			return path;
		}


		/**
		 * @param path the path to set
		 */
		public void setPath(List<String> path) {
			this.path = path;
		}


		/**
		 * @return the iteration
		 */
		public String getIteration() {
			return iteration;
		}


		/**
		 * @param iteration the iteration to set
		 */
		public void setIteration(String iteration) {
			this.iteration = iteration;
		}


		/**
		 * @return the iic
		 */
		public int getIic() {
			return iic;
		}


		/**
		 * @param iic the iic to set
		 */
		public void setIic(int iic) {
			this.iic = iic;
		}


		/**
		 * @return the collectionRef
		 */
		public String getCollectionRef() {
			return collectionRef;
		}


		/**
		 * @param collectionRef the collectionRef to set
		 */
		public void setCollectionRef(String collectionRef) {
			this.collectionRef = collectionRef;
		}


		/**
		 * @return the proc
		 */
		public String getProc() {
			return proc;
		}


		/**
		 * @param proc the proc to set
		 */
		public void setProc(String proc) {
			this.proc = proc;
		}


		/**
		 * @return the var
		 */
		public String getVar() {
			return var;
		}


		/**
		 * @param var the var to set
		 */
		public void setVar(String var) {
			this.var = var;
		}


		/**
		 * @return the varType
		 */
		public String getVarType() {
			return varType;
		}


		/**
		 * @param varType the varType to set
		 */
		public void setVarType(String varType) {
			this.varType = varType;
		}


		/**
		 * @return the wfInstance
		 */
		public String getWfInstance() {
			return wfInstance;
		}


		/**
		 * @param wfInstance the wfInstance to set
		 */
		public void setWfInstance(String wfInstance) {
			this.wfInstance = wfInstance;
		}


		/**
		 * @return the isXform
		 */
		public boolean isXform() {
			return isXform;
		}


		/**
		 * @param isXform the isXform to set
		 */
		public void setXform(boolean isXform) {
			this.isXform = isXform;
		}



		/**
		 * @return the collectionNesting
		 */
		public int getCollectionNesting() {
			return collectionNesting;
		}


		/**
		 * @param collectionNesting the collectionNesting to set
		 */
		public void setCollectionNesting(int collectionNesting) {
			this.collectionNesting = collectionNesting;
		}


		/**
		 * @return the iterationVector
		 */
		public String getIterationVector() {
			return iterationVector;
		}


		/**
		 * @param iterationVector the iterationVector to set
		 */
		public void setIterationVector(String iterationVector) {
			this.iterationVector = iterationVector;
		}


		/**
		 * @return the dNL
		 */
		public int getDNL() {
			return DNL;
		}


		/**
		 * @param dnl the dNL to set
		 */
		public void setDNL(int dnl) {
			DNL = dnl;
		}


		/**
		 * @return the aNL
		 */
		public int getANL() {
			return ANL;
		}


		/**
		 * @param anl the aNL to set
		 */
		public void setANL(int anl) {
			ANL = anl;
		}
	}


}
