/**
 * 
 */
package net.sf.taverna.t2.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
		for (Map.Entry entry:annotations.entrySet())  {
			
			System.out.println("annotations for proc "+entry.getKey());
			for (String ann: (List<String>) entry.getValue()) {
				System.out.println(ann);
			}
		}
		
	}


	public List<String> getWFInstanceIDs() throws SQLException {

		return pq.getWFInstanceIDs();

	}


	public void computeLineagePaths(
			String workflowID,   // context
			String var,   // target var
			String proc,   // qualified with its processor name
			int targetIteration, 
			Set<String> selectedProcessors
	) throws SQLException  {

		LineageAnnotation aLA = new LineageAnnotation();
		aLA.setIteration(targetIteration);
		aLA.setProc(proc);
		aLA.setVar(var);
		aLA.setWfInstance(workflowID);

		List<LineageAnnotation> initialPath = new ArrayList<LineageAnnotation>();
		initialPath.add(aLA);

		List<LineageSQLQuery>  lqList = new ArrayList<LineageSQLQuery>();  // holds the output

		// initial recursive call
		computeLineagePaths(workflowID, var, proc, selectedProcessors, initialPath, lqList);

		// execute queries in the LineageSQLQuery list
		pq.runLineageQueries(lqList);

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
	 * @param targetIteration 
	 * @return a list of paths. Each path is a list of strings, each string represents one annotated step that can be used 
	 * for query generation in phase II
	 * @throws SQLException 
	 */
	public void computeLineagePaths(
			String workflowID,   // context
			String var,   // starting var
			String proc,   // qualified with its processor name
			Set<String>  selectedProcessors, // processors where the paths end
			List<LineageAnnotation> initialPath,
			List<LineageSQLQuery>  lqList
	) throws SQLException  {

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

		Var v = vars.get(0); 		// expect one record

		List<Var> nextVars = new ArrayList<Var>();

		boolean xform = false;
		if (v.isInput()) {
			// if vName is input, then do a xfer() step

			// retrieve all Arcs ending with (var,proc)
			Map<String, String>  arcsQueryConstraints = new HashMap<String, String>();

			arcsQueryConstraints.put("wfInstanceRef", workflowID);
			arcsQueryConstraints.put("sinkVarNameRef", var);  
			arcsQueryConstraints.put("sinkPNameRef", proc);  

			List<Arc> arcs = pq.getArcs(arcsQueryConstraints);

			for (Arc a: arcs) {
				// get source node
				String sourceProc = a.getSourcePnameRef();
				String sourceVar  = a.getSourceVarNameRef();

				// get this var from DB -- this creates a Var object that contains static type info
				// retrieve all input Vars to the current processor
				Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

				varsQueryConstraints.put("wfInstanceRef", workflowID);
				varsQueryConstraints.put("pnameRef", sourceProc);  
				varsQueryConstraints.put("varName", sourceVar);  

				List<Var> inputVars = pq.getVars(varsQueryConstraints);

				nextVars.addAll(inputVars);
			}


		} else {
			xform = true;

			// if vName is output, then do a xform() step

			// retrieve all input Vars to the current processor
			Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

			varsQueryConstraints.put("wfInstanceRef", workflowID);
			varsQueryConstraints.put("pnameRef", proc);  
			varsQueryConstraints.put("inputOrOutput", "1");  // FIXME: 1 = input??  CHECK  

			nextVars = pq.getVars(varsQueryConstraints);

		}

		// now process the collected set of new vars in depth-first
		int i=0;
		for (Var nextVar: nextVars) {

			String procName = nextVar.getPName();
			String varName  = nextVar.getVName();
			String varType  = nextVar.getType();

			if (xform) {
				System.out.println("xform() from ["+v.getPName()+","+v.getVName()+"] to ["+procName+","+varName+"]");
			} else {
				System.out.println("xfer() from ["+v.getPName()+","+v.getVName()+"] to ["+procName+","+varName+"]");
			}

			// produce annotation and add it to the current path
			LineageAnnotation LA = new LineageAnnotation();

			if (xform) LA.setXform(true); else LA.setXform(false);

			LA.setWfInstance(workflowID);
			LA.setProc(procName);
			LA.setVar(varName);

			if (varType == null) {
				System.out.println("WARNING: no type info available for ["+procName+"/"+varName+"] -- assuming s");
				varType = "s";
			}
			LA.setVarType(varType);
			LA.setVarTypeNestingLevel(nextVar.getTypeNestingLevel());	//varTypeNestingLevel

			initialPath.add(LA);
			

			// **************
			// process currentPath
			// **************
			annotateLineageStep(initialPath, annotations);

			// generate a query if required
			if (selectedProcessors.isEmpty() || selectedProcessors.contains(procName)) {

				// processing done only on input vars -- this means at the end of a xform() step
				// unless this is a xfer() into INPUT, in this case the output var of _INPT_ is also processed
				if (xform || procName.equals("_INPUT_")) {

					System.out.println("QueryGen for ["+procName+"] will see:  ");

					int tabs = 0;
					for (Iterator<LineageAnnotation> lIt = initialPath.iterator(); lIt.hasNext(); tabs++) {
						String indent = new String();
						for (int j=0; j<tabs; j++) { indent = indent+"\t"; }
						System.out.println(indent+lIt.next().toString());
					}

					LineageSQLQuery lq = pq.LineageQueryGen(initialPath);

					lqList.add(lq);

					System.out.println("SQL query:\n"+lq.getSQLQuery());

				}

			}

			// recursive call
			computeLineagePaths(workflowID, varName, procName, selectedProcessors, initialPath, lqList);				

			initialPath.remove(initialPath.size()-1);

		}

		return;

	}


	/**
	 * updates the annotation in the last node of the input path by applying 
	 * the query rewriting rules to the path. Ideally, the rules are all local i.e., they only involve the node before the last. 
	 * The entire path is available, however, should it be needed 
	 * @param path
	 * @param annotations 
	 */
	protected void annotateLineageStep(List<LineageAnnotation> path, Map<String, List<String>> annotations) {

		// look up the applicable rule using the varType information for the last two nodes

		if (path.size() < 2)  {
			System.out.println("not enough nodes in the path to apply a rule - terminating");
			return;
		}

		LineageAnnotation prevNode = path.get(path.size()-2); // previous
		LineageAnnotation currNode = path.get(path.size()-1); // current

		int fromNesting = currNode.getVarTypeNestingLevel();
		int toNesting   = prevNode.getVarTypeNestingLevel();

		boolean isXform = currNode.isXform();

		//
		// xform rules
		//
		if (isXform)  { // apply xform() rules

			if (fromNesting == 0 && toNesting == 0)  { // s -> s see rule xform/1

				currNode.setIteration(prevNode.getIteration());
				currNode.setIic(prevNode.getIic());
				currNode.setCollectionNesting(prevNode.getCollectionNesting());

				System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> s rule applied");

			}  else if (fromNesting < toNesting) {  // s -> l(s) lossless -- see rule xform/2

				currNode.setIteration(prevNode.getIteration());
				currNode.setIic(0);

				System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> l(s) rule applied");

			} else if (fromNesting > toNesting) {  // l(s) -> s  (loss of precision) see rule xform/3

				
				currNode.setIteration(prevNode.getIteration());
				currNode.setCollectionNesting(prevNode.getCollectionNesting()+1);

				System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> s rule applied");

			} else {  // l(s) -> l(s) see rule xform/4  

				// retrieve processor annotations at this point
				// if P is index-preserving, apply specialised rule

				String procName = currNode.getProc();

				List<String> annotList = null;
				if ( annotations != null && annotations.containsKey(procName) && annotations.get(procName).contains(IP_ANNOTATION)) {

					System.out.println(procName+" is "+IP_ANNOTATION);
					
					currNode.setIteration(prevNode.getIteration());
					currNode.setIic(prevNode.getIic());
					currNode.setCollectionNesting(prevNode.getCollectionNesting());
					
					System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) special rule applied");
					
				}  else {  // default rule, no annotation

					currNode.setCollectionNesting(prevNode.getCollectionNesting()+1);
					currNode.setIic(0);

					System.out.println("xform (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) regular rule applied");

				}

			}

			// 
			// xfer rules
			//
		} else if (fromNesting == 0 && toNesting == 0)  { // s -> s see rule xfer/1

			currNode.setIteration(prevNode.getIteration());
			currNode.setIic(prevNode.getIic());
			currNode.setCollectionNesting(prevNode.getCollectionNesting());

			System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> s rule applied");

		} else if (fromNesting < toNesting) {  // s -> l(s) lossless -- see rule xfer/2

			currNode.setCollectionNesting(prevNode.getCollectionNesting() - 1);
			currNode.setIteration(prevNode.getIteration());
			currNode.setIic(prevNode.getIic());

			System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- s -> l(s) rule applied");

		} else if (fromNesting > toNesting) {  // l(s) -> s  (loss of precision) see rule xfer/3

			// case of implicit iteration

			currNode.setIteration(prevNode.getIic());
			currNode.setCollectionNesting(prevNode.getCollectionNesting());
			// currNode.setIic(prevNode.getIic());  // ?? unclear

			System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> s rule applied");

		} else { // l(s) -> l(s) see rule xfer/4 

			currNode.setIteration(prevNode.getIteration());
			currNode.setIic(prevNode.getIic());
			currNode.setCollectionNesting(prevNode.getCollectionNesting());

			System.out.println("xfer (fromNesting, toNesting) = ("+fromNesting+","+toNesting+") -- l(s) -> l(s) rule applied");

		}

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

		int iteration = 0;  // default is 0
		int iic = 0;  // index in collection -- default is 0 
		int collectionNesting = 0;  // n indicates granularity is n levels from leaf. 
		// This quantifies loss of lineage precision when working with collections
		String collectionRef = null;
		String proc;
		String var;
		String varType = null;   // dtring, XML,... see Taverna type system
		int varTypeNestingLevel = 0; // default: depth = 0 in type signature (atomic data)
		String wfInstance;  // TODO generalize to list / time interval?

		public String toString() {

			StringBuffer sb = new StringBuffer();

			if (isXform)  sb.append(" xform: ");
			else sb.append(" xfer: ");

			sb.append("<PROC/VAR/VARTYPE, IT, IIC, COLLNESTING> = "+
					proc + "/" + var + "/" + varType +
					"," + iteration +
					","+ iic + 
					","+ varTypeNestingLevel);

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
		public int getIteration() {
			return iteration;
		}


		/**
		 * @param iteration the iteration to set
		 */
		public void setIteration(int iteration) {
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
		 * @return the varTypeNestingLevel
		 */
		public int getVarTypeNestingLevel() {
			return varTypeNestingLevel;
		}


		/**
		 * @param varTypeNestingLevel the varTypeNestingLevel to set
		 */
		public void setVarTypeNestingLevel(int varTypeNestingLevel) {
			this.varTypeNestingLevel = varTypeNestingLevel;
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
	}


}
