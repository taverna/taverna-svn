/**
 * 
 */
package net.sf.taverna.t2.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.service.util.Arc;
import net.sf.taverna.t2.service.util.Var;

/**
 * @author paolo<p/>
 * the main class for querying the lineage DB
 * assumes a provenance DB ready to be queried
 */
public class ProvenanceAnalysis {

	private static final Object IP_ANNOTATION = "index-preserving";

	ProvenanceQuery pq = null;
	AnnotationsLoader al = null;

	Map<String,List<String>> annotations = null;  // user-made annotations to processors

	public ProvenanceAnalysis() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		pq = new ProvenanceQuery();		

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


	public List<String> getWFInstanceIDs() throws SQLException {

		return pq.getWFInstanceIDs();

	}


	/**
	 * new compute lineage path accounts for cross products
	 * @param workflowID
	 * @param var
	 * @param proc
	 * @param targetIteration
	 * @param selectedProcessors
	 * @throws SQLException
	 */
	public void clp2(
			String workflowID,   // context
			String var,   // target var
			String proc,   // qualified with its processor name
			String targetIteration, 
			Set<String> selectedProcessors
	) throws SQLException  {

		// initialize annotated path
		LineageAnnotation aLA = new LineageAnnotation();
		aLA.setIteration(targetIteration);
		aLA.setProc(proc);
		aLA.setVar(var);
		aLA.setWfInstance(workflowID);

		List<LineageAnnotation> initialPath = new ArrayList<LineageAnnotation>();
		initialPath.add(aLA);

		List<LineageSQLQuery>  lqList = new ArrayList<LineageSQLQuery>();  // holds the output

		// start with xfer or xform depending on whether initial var is output or input

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", workflowID);
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
			xferStep(workflowID, var, proc, null, selectedProcessors, initialPath, lqList);


		} else { // start with xform

			// rec. accumulates SQL queries into lqList
			xformStep(workflowID, var, proc, selectedProcessors, initialPath, lqList);

		}

		// execute queries in the LineageSQLQuery list
		pq.runLineageQueries(lqList);

	}




	/**
	 * current var is a processor output var for sure when we get here
	 * @param workflowID
	 * @param outputVar
	 * @param proc
	 * @param selectedProcessors
	 * @param initialPath  grows at each step along the path
	 * @param lqList
	 * @throws SQLException
	 */
	public void xformStep(
			String workflowID,   // context
			String outputVar,   // starting var
			String proc,   // qualified with its processor name
			Set<String>  selectedProcessors, // processors where the paths end
			List<LineageAnnotation> initialPath,
			List<LineageSQLQuery>  lqList) throws SQLException  {

		/////////////
		// xform step
		/////////////

		// retrieve input vars for current processor 
		Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

		varsQueryConstraints.put("wfInstanceRef", workflowID);
		varsQueryConstraints.put("pnameRef", proc);  
		varsQueryConstraints.put("inputOrOutput", "1");  

		List<Var> inputVars = new ArrayList<Var>();

		inputVars = pq.getVars(varsQueryConstraints);

		Map<Var,Var>  sink2source  = new HashMap<Var,Var> ();  // sink node -> source node for an arc 
		
		///////
		// lookahead: fetch source vars from the Arcs . This will be used in the xfer step
		///////
		
		int inputVarCnt0 = 0;
		for (Var inputVar: inputVars) {

			String procName      = inputVar.getPName();
			String inputVarName  = inputVar.getVName();
			
			// retrieve all Arcs ending with (var,proc) -- expect exactly one, since no multiple incoming arcs allowed
			Map<String, String>  arcsQueryConstraints = new HashMap<String, String>();

			arcsQueryConstraints.put("wfInstanceRef", workflowID);
			arcsQueryConstraints.put("sinkVarNameRef", inputVarName);  
			arcsQueryConstraints.put("sinkPNameRef", procName);  

			List<Arc> arcs = pq.getArcs(arcsQueryConstraints);

			// we expect at most one arc for each nextVar
			Arc a = arcs.get(0);
			if (a != null) {
				
				// get source node for this arc
				String sourceProcName = a.getSourcePnameRef();
				String sourceVarName  = a.getSourceVarNameRef();

				// retrieve detail for the source var for this arc 
				varsQueryConstraints = new HashMap<String, String>();

				varsQueryConstraints.put("wfInstanceRef", workflowID);
				varsQueryConstraints.put("pnameRef", sourceProcName);  
				varsQueryConstraints.put("varName", sourceVarName);  

				List<Var> sourceVars = pq.getVars(varsQueryConstraints);

				// expect exactly one source var
				Var sourceVar = sourceVars.get(0);

				sink2source.put(inputVar, sourceVar);
				
				inputVarCnt0++;
			}
			
		}  // end LOOKAHEAD for each input var
		
		
		// determine the chunks of iteration vector that need to be allocated to each input var
		// this is based on the DNL-ANL analysis
		LineageAnnotation prevNode = initialPath.get(initialPath.size()-2); // previous: refers to one input var of the processor

		String iterationVector[] = prevNode.getIteration().split(",");
		
		Map<Var,String> var2ITVector = new HashMap<Var,String>();
		
		String[] iterationElements; 
		
		int start = 0;
		for (Var inputVar: inputVars) {
			
			// 24/7/08 get DNL (declared nesting level) and ANL (actual nesting level) from VAR
			int projectedIterationLength = inputVar.getActualNestingLevel() - inputVar.getTypeNestingLevel();			

			if (projectedIterationLength > 0) {
				
				iterationElements = new String[projectedIterationLength];
				
				for (int i=0; i<projectedIterationLength; i++) {
					
					iterationElements[i] = iterationVector[start+i];
				}		
				start += projectedIterationLength;
				
				StringBuffer iterationFragment = new StringBuffer();
				for (String s:iterationElements) { iterationFragment.append(s+","); }
				iterationFragment.deleteCharAt(iterationFragment.length()-1);
				
				var2ITVector.put(inputVar, iterationFragment.toString());
			}
			
		}

		// perform xform step on each input var separately and update the annotated path -- depth-first recursion
		
		for (Var inputVar: inputVars) {

			System.out.println("xform() from ["+proc+","+outputVar+"] to ["+inputVar.getPName()+","+inputVar.getVName()+"]");

			// produce annotation and add it to the current path
			LineageAnnotation LA = new LineageAnnotation();

			LA.setXform(true);
			LA.setWfInstance(workflowID);
			LA.setProc(inputVar.getPName());
			LA.setVar(inputVar.getVName());
			
			String varType = inputVar.getType();
			if (varType == null) {
				System.out.println("WARNING: no type info available for ["+inputVar.getPName()+"/"+inputVar.getVName()+"] -- assuming s");
				varType = "s";
			}
			LA.setVarType(inputVar.getType());
			LA.setDNL(inputVar.getTypeNestingLevel());	//varTypeNestingLevel	
			LA.setCollectionNesting(inputVar.getTypeNestingLevel());  

			initialPath.add(LA);

			// **************
			// process currentPath
			// **************
			
			// updates the path
			List<LineageAnnotation>  newPath = applyXformRule(initialPath, inputVars, var2ITVector.get(inputVar), annotations);								

			// generate SQL if necessary -- this assumes SQL can be generated individually for each input var
			if (selectedProcessors.isEmpty() || selectedProcessors.contains(proc)) {

				// unless this is a xfer() into INPUT, in this case the output var of _INPUT_ is also processed
				System.out.println("QueryGen for ["+proc+"] will see:  ");

				int tabs = 0;
				for (Iterator<LineageAnnotation> lIt = newPath.iterator(); lIt.hasNext(); tabs++) {
					String indent = new String();
					for (int j=0; j<tabs; j++) { indent = indent+"\t"; }
					System.out.println(indent+lIt.next().toString());
				}

				LineageSQLQuery lq = pq.LineageQueryGen(newPath);

				lqList.add(lq);

				System.out.println("SQL query:\n"+lq.getSQLQuery());

			}

			// retrieve the arc source var cached earlier
			Var sourceVar = sink2source.get(inputVar);
			
			// recurse using the new path
			xferStep(workflowID, inputVar.getVName(), inputVar.getPName(), sourceVar, selectedProcessors, initialPath, lqList);
			
			initialPath.remove(initialPath.size()-1);

		}  // end for 
}


	/**
	 * a xfer step involves one single var 
	 * @param workflowID
	 * @param var this var is a source of an arc
	 * @param proc
	 * @param selectedProcessors
	 * @param initialPath
	 * @param lqList
	 * @return
	 * @throws SQLException
	 */
	public void xferStep(String workflowID,   // context
			String var,   // starting var
			String proc,   // qualified with its processor name
			Var sourceVar,  // if null, this avoids a new DB lookup
			Set<String>  selectedProcessors, // processors where the paths end
			List<LineageAnnotation> initialPath,  // path from initial var to current processor
			List<LineageSQLQuery>  lqList) throws SQLException {

		
		String sourceProcName = null;
		String sourceVarName  = null;
		String sourceVarType  = null;
		int sourceNestingLevel = 0;
		
		// sourceVar is the var we need to retrieve. avoid lookup if provided
		if (sourceVar != null) {
			
			sourceVarName  = sourceVar.getVName();
			sourceProcName = sourceVar.getPName();
			sourceVarType  = sourceVar.getType();
			sourceNestingLevel = sourceVar.getTypeNestingLevel();
			
		} else {  // DB lookup required
			
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
		}
		
		System.out.println("xfer() from ["+proc+","+var+"] to ["+sourceProcName+","+sourceVarName+"]");

		// produce annotation and add it to the current path
		LineageAnnotation LA = new LineageAnnotation();

		LA.setXform(false);
		LA.setWfInstance(workflowID);
		LA.setProc(sourceProcName);
		LA.setVar(sourceVarName);

		if (sourceVarType == null) {
			System.out.println("WARNING: no type info available for ["+sourceProcName+"/"+sourceVarName+"] -- assuming s");
			sourceVarType = "s";
		}
		LA.setVarType(sourceVarType);
		LA.setDNL(sourceNestingLevel);	//varTypeNestingLevel

		initialPath.add(LA);

		// **************
		// process currentPath
		// **************

		applyXferRule(initialPath, annotations);

		// recurse on xform
		xformStep(workflowID, sourceVarName, sourceProcName, selectedProcessors, initialPath, lqList);
		
		initialPath.remove(initialPath.size()-1);
		// remove last annotation in path
		
		return; 
	}


/**
 * 
 * @param path the path being updated
 * @param inputVars  the entire set of input variables to the processor that does the transformation. 
 * These are needed in order to apportion elements of an iteration vector to corresponding individual inputs.<br/>
 * This step involves exactly one output var for a processor P, and one input var for the same P
 * @param projectedIterationLength 
 * @param var2NL 
 * @param annotations
 */
protected List<LineageAnnotation> applyXformRule(List<LineageAnnotation> path, 
		                                         List<Var> inputVars, 
		                                         String iterationVectorFragment,
		                                         Map<String, 
		                                         List<String>> annotations) {

	// look up the applicable rule using the DNL and ANL information for the last two nodes

	if (path.size() < 2)  {
		System.out.println("not enough nodes in the path to apply a rule - terminating");
		return path;
	}

	LineageAnnotation prevNode = path.get(path.size()-2); // previous: refers to one input var of the processor
	LineageAnnotation currNode = path.get(path.size()-1); // current: refers to one output var of the processor

	int fromNesting = currNode.getDNL();
	
	int toNesting   = prevNode.getDNL();
	
	// first deal with iteration issues
	// is this processor iterating over its inputs?
	System.out.println("iterationVectorFragment: "+iterationVectorFragment);
	
	if (iterationVectorFragment == null) {  // this input is not involved in iteration at all
		
		System.out.println("no iteration involved");
		currNode.setIteration(prevNode.getIteration());  // propagate any iteration request upwards with no change
		
	} else { // this input is involved in iteration -- use iterationVectorFragment 
		
		currNode.setIteration(iterationVectorFragment);
		
	}

	// also save full iteration vector (if any) so it can be used if a query is generated at the end of this xform() step
	currNode.setIterationVector(prevNode.getIteration());
	
	// now apply NL mismatch rules 
	if (fromNesting == 0 && toNesting == 0)  { // s -> s see rule xform/1
		
		currNode.setIic(prevNode.getIic());
		currNode.setCollectionNesting(prevNode.getCollectionNesting());

		System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> s rule applied");

	}  else if (fromNesting < toNesting) {  // s -> l(s) lossless -- see rule xform/2

//		currNode.setIteration(prevNode.getIteration());
		currNode.setIic(0);
		currNode.setCollectionNesting(prevNode.getCollectionNesting()-1);

		System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> l(s) rule applied");

	} else if (fromNesting > toNesting) {  // l(s) -> s  (loss of precision) see rule xform/3

//		currNode.setIteration(prevNode.getIteration());
		currNode.setCollectionNesting(prevNode.getCollectionNesting()+1);  // increase nesting level --> point to enclosing collection

		System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> s rule applied");

	} else {  // l(s) -> l(s) see rule xform/4  

		// retrieve processor annotations at this point
		// if P is index-preserving, apply specialised rule

		String procName = currNode.getProc();

		if ( annotations != null && annotations.containsKey(procName) && annotations.get(procName).contains(IP_ANNOTATION)) {

			System.out.println(procName+" is "+IP_ANNOTATION);

//			currNode.setIteration(prevNode.getIteration());
			currNode.setIic(prevNode.getIic());
			currNode.setCollectionNesting(prevNode.getCollectionNesting());

			System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) special rule applied");

		}  else {  // default rule, no annotation

			currNode.setCollectionNesting(prevNode.getCollectionNesting()+1);
			currNode.setIic(0);

			System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) regular rule applied");

		}

	}

	return path;

}



/**
 * updates the annotation in the last node of the input path by applying 
 * the query rewriting rules to the path. Ideally, the rules are all local i.e., they only involve the node before the last. 
 * The entire path is available, however, should it be needed 
 * @param path the list of LineageAnnotation to be updated
 * @param annotations an optional list of processor annotations. These may alter the propagation rules
 */
protected List<LineageAnnotation>  applyXferRule(List<LineageAnnotation> path, Map<String, List<String>> annotations) {

	// look up the applicable rule using the varType information for the last two nodes

	if (path.size() < 2)  {
		System.out.println("not enough nodes in the path to apply a rule - terminating");
		return path;
	}

	LineageAnnotation prevNode = path.get(path.size()-2); // previous
	LineageAnnotation currNode = path.get(path.size()-1); // current

	int fromNesting = currNode.getDNL();
	int toNesting   = prevNode.getDNL();

	if (fromNesting == 0 && toNesting == 0)  { // s -> s see rule xfer/1

		currNode.setIterationVector(prevNode.getIteration());  // old iteration becomes new iteration vector CHECK
		currNode.setIteration(prevNode.getIteration());  // old iteration becomes new iteration vector CHECK
		
		currNode.setIic(prevNode.getIic());
		currNode.setCollectionNesting(prevNode.getCollectionNesting());

		System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> s rule applied");

	} else if (fromNesting < toNesting) {  // s -> l(s) lossless -- see rule xfer/2

		if (prevNode.getCollectionNesting() > 0 )
			currNode.setCollectionNesting(prevNode.getCollectionNesting() - 1);
		else currNode.setCollectionNesting(0);
		
		currNode.setIteration(prevNode.getIteration());
		currNode.setIic(prevNode.getIic());

		System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> l(s) rule applied");

	} else if (fromNesting > toNesting) {  // l(s) -> s  (from list to iteration) see rule xfer/3

		// case of implicit iteration

		currNode.setIic(Integer.parseInt(prevNode.getIteration())); // CHECK
		
//		currNode.setIteration(Integer.toString(prevNode.getIic()));  // 
		currNode.setCollectionNesting(prevNode.getCollectionNesting());

		System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> s rule applied");

	} else { // l(s) -> l(s) see rule xfer/4 

	//	currNode.setIteration(prevNode.getIteration());
		currNode.setIic(prevNode.getIic());
		currNode.setCollectionNesting(prevNode.getCollectionNesting());

		System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) rule applied");

	}

	return path;
	// FIXME varType not yet available, so assume s -> s throughout just for the sake of testing
}



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
