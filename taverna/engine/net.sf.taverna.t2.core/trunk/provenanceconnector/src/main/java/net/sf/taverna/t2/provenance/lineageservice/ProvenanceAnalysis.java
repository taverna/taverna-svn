/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.provenance.lineageservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.provenance.api.NativeAnswer;
import net.sf.taverna.t2.provenance.api.QueryAnswer;
import net.sf.taverna.t2.provenance.lineageservice.utils.DataLink;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.QueryPort;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowInstance;
import net.sf.taverna.t2.provenance.opm.OPMManager;

import org.apache.log4j.Logger;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.provenance.ProvenanceArtifact;
import org.tupeloproject.provenance.ProvenanceRole;

/**
 * @author paolo<p/>
 * the main class for querying the lineage DB
 * assumes a provenance DB ready to be queried
 */
public class ProvenanceAnalysis {

	private static Logger logger = Logger.getLogger(ProvenanceAnalysis.class);

	private static final String IP_ANNOTATION = "index-preserving";
	private static final String OUTPUT_CONTAINER_PROCESSOR = "_OUTPUT_";
	private static final String INPUT_CONTAINER_PROCESSOR = "_INPUT_";
	public static final String ALL_PATHS_KEYWORD = "ALL";

	private ProvenanceQuery pq = null;
	private AnnotationsLoader al = new AnnotationsLoader();  // singleton

	// paths collected by lineageQuery and to be used by naive provenance query
	private Map<ProvenanceProcessor, List<List<String>>> validPaths = new HashMap<ProvenanceProcessor, List<List<String>>>();

	private List<String> currentPath;
	private Map<String,List<String>> annotations = null;  // user-made annotations to processors

	private boolean ready = false; // set to true as soon as init succeeds. this means pa is ready to answer queries

	private boolean returnOutputs = false; // by default only return input bindings

	private boolean includeDataValue = false; // forces the lineage queries to return de-referenced data values

	private boolean generateOPMGraph = true;

	// TODO extract this to prefs -- selects which OPMManager is to be used to export to OPM
	private String OPMManagerClass = "net.sf.taverna.t2.provenance.lineageservice.ext.pc3.PANSTARRSOPMManager";

	private OPMManager aOPMManager = null;

	private boolean recordArtifactValues = false;

	public ProvenanceAnalysis() { ; }

	public ProvenanceAnalysis(ProvenanceQuery pq) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this.pq = pq;
		setReady(tryInit());
	}


	private boolean tryInit() throws SQLException {

		if (getWFInstanceIDs() != null && getWFInstanceIDs().size()>0) {
			initGraph();  // init OPM provenance graph
			return true;
		} else 
			return false;		
	}

	/**
	 * Call to create the opm graph and annotation loader. 
	 * this may fail due to queries being issued before DB is populated, minimally with wfInstanceID 
	 */
	public void initGraph() {

		// OPM management
		try {
			aOPMManager  = (OPMManager) Class.forName(OPMManagerClass).newInstance();
		} catch (InstantiationException e1) {
			logger.error("Problem initialising opm graph: ",  e1);
		} catch (IllegalAccessException e1) {
			logger.error("Problem initialising opm graph: ", e1);
		} catch (ClassNotFoundException e1) {
			logger.info("chosen OPMmanager: "+OPMManagerClass+" not available, reverting to default");
			aOPMManager  = new OPMManager();
		}

		try {
			aOPMManager.createAccount(getWFInstanceIDs().get(0).getInstanceID());
		} catch (SQLException e) {
			logger.error("Could not initialise OPM Manager: ", e);
		}
	}


	/**
	 * asks the OPM manager to convert its current RDF OPMGraph to XML 
	 * @return the filename of the OPM XML file
	 * @throws OperatorException
	 * @throws IOException
	 * @throws JAXBException
	 */
//	public String OPMRdf2Xml() throws OperatorException, IOException, JAXBException {
//	if (isReady()) {
//	return aOPMManager.Rdf2Xml();
//	}  
//	return null;
//	}

	/**
	 * asks the OPM manager to create a dot file representing its current RDF OPMGraph<br/>
	 * needs fixing
	 * @return
	 * @throws IOException 
	 * @throws OperatorException 
	 */
	public String OPMRdf2Dot() throws OperatorException, IOException {
		if (aOPMManager != null && aOPMManager.isActive() && isReady()) {
			return aOPMManager.Rdf2Dot();
		}  
		return null;		
	}


	public void setAnnotationFile(String annotationFile) {

		annotations = al.getAnnotations(annotationFile);

		if (annotations == null) {
			logger.warn("no annotations have been loaded");
			return;
		}

		logger.info("processor annotations for lineage refinement: ");
		for (Map.Entry<String,List<String>> entry:annotations.entrySet())  {

			logger.info("annotations for proc "+entry.getKey());
			for (String ann: (List<String>) entry.getValue()) {
				logger.info(ann);
			}
		}

	}


	/**
	 * returns all available instances across all workflows
	 * @return
	 * @throws SQLException
	 */
	public List<WorkflowInstance> getWFInstanceIDs() throws SQLException { return getPq().getRuns(null, null); }


	/**
	 * returns all available instances for workflow wfName
	 *@param wfName
	 * @return
	 * @throws SQLException
	 */
	public List<WorkflowInstance> getWFInstanceID(String wfName) throws SQLException { 
		return getPq().getRuns(wfName, null); }


	/**
	 * @param wfInstance lineage scope -- a specific instance
	 * @param pname for a specific processor [required]
	 * @param a specific (input or output) variable [optional]
	 * @param iteration and a specific iteration [optional]
	 * @param wfNameRef 
	 * @return a lineage query ready to be executed, or null if we cannot return an answer because we are not ready
	 * (for instance the DB is not yet populated) 
	 * @throws SQLException
	 */
	public Dependencies fetchIntermediateResult(
			String wfInstance,
			String wfNameRef,
			String pname,
			String vname,
			String iteration) throws SQLException  {

		if (!isReady()) {
			setReady(tryInit());
			if (!isReady())  return null;
		}


		LineageSQLQuery lq = getPq().simpleLineageQuery(wfInstance, wfNameRef, pname, vname, iteration);

		return getPq().runLineageQuery(lq, isIncludeDataValue());
	}



	public QueryAnswer lineageQuery(List<QueryPort> qvList,
			String wfInstance, List<ProvenanceProcessor> selectedProcessors) throws SQLException {

		QueryAnswer  completeAnswer = new QueryAnswer();
		NativeAnswer nativeAnswer   = new NativeAnswer();

		Map<QueryPort, Map<String, List<Dependencies>>> answerContent = new HashMap<QueryPort, Map<String, List<Dependencies>>>();

		// launch a lineage query for each target variable
		for (QueryPort qv:qvList) {

			// full lineage query			
			logger.info("************\n lineage query: [instance, workflow, proc, port, path] = ["+
					wfInstance+","+qv.getWfName()+","+qv.getPname()+","+qv.getVname()+",["+qv.getPath()+"]]\n***********");

			// the OPM manager builds an OPM graph behind the scenes as a side-effect
			Map<String, List<Dependencies>> a = 
				computeLineageSingleVar(wfInstance, qv.getWfName(), qv.getVname(), qv.getPname(), qv.getPath(), selectedProcessors);

			answerContent.put(qv, a);
		}

		nativeAnswer.setAnswer(answerContent);
		completeAnswer.setNativeAnswer(nativeAnswer);

		if (aOPMManager != null && aOPMManager.isActive())  {
//			String _OPM_asXML_File;
//			try {

//			_OPM_asXML_File = aOPMManager.Rdf2Xml();
			String _OPM_asRDF_File = aOPMManager.writeGraph();

			completeAnswer.setOPMAnswer_AsRDF(_OPM_asRDF_File);
//			completeAnswer.setOPMAnswer_AsXML(_OPM_asXML_File);

//			} catch (OperatorException e) {
//			logger.error("Problem running query: " + e);
//			} catch (IOException e) {
//			logger.error("Problem running query: " + e);
//			} catch (JAXBException e) {
//			logger.error("Problem running query: " + e);
//			}
		}
		return completeAnswer;
	}

	/**
	 * facade for computeLineage: if path == ALL then it retrieves all VBs for (proc,var) ignoring path
	 * (i.e., all values within the collection bound to var) and invokes computeLineageSingleBinding() on each path</br>
	 * if path is specified, however, this just passes the request to computeLineageSingleBinding. in this case the result map 
	 * only contains one entry
	 * @param wfInstance
	 * @param var
	 * @param proc
	 * @param path
	 * @param string 
	 * @param selectedProcessors
	 * @return a map <pre>{ path -> List<LineageQueryResult> }</pre>, one entry for each path
	 * @throws SQLException
	 */
	public Map<String, List<Dependencies>> computeLineageSingleVar (
			String wfInstance,   // dynamic scope 
			String wfNameRef,    // static scope
			String var,   // target var
			String proc,   // qualified with its processor name
			String path,   // possibly empty when no collections or no granular lineage required
			List<ProvenanceProcessor> selectedProcessors
	) throws SQLException  {

		if (!isReady()) {
			setReady(tryInit());
			if (!isReady())  return null;
		}

		// are we returning all outputs in addition to the inputs?
		logger.info("return outputs: "+isReturnOutputs());

		Map<String, List<Dependencies>> qa = new HashMap<String, List<Dependencies>>();

		// run a query for each variable in the entire workflow graph
		if (path.equals(ALL_PATHS_KEYWORD)) {

			Map<String, String> vbConstraints = new HashMap<String, String>();
			vbConstraints.put("VB.PNameRef", proc);
			vbConstraints.put("VB.varNameRef", var);
			vbConstraints.put("VB.wfInstanceRef", wfInstance);

			List<PortBinding> vbList = getPq().getPortBindings(vbConstraints); // DB

			if (vbList.isEmpty()) {
				logger.warn(ALL_PATHS_KEYWORD+" specified for paths but no varBindings found. nothing to compute");
			}

			for (PortBinding vb:vbList) {

				// path is of the form [x,y..]  we need it as x,y... 
				path = vb.getIteration().substring(1, vb.getIteration().length()-1);

				List<Dependencies> result = computeLineageSingleBinding(
						wfInstance, wfNameRef, var, proc, path, selectedProcessors);
				qa.put(vb.getIteration(), result);
			}
		}  else {
			qa.put(path, computeLineageSingleBinding(
					wfInstance, wfNameRef, var, proc, path, selectedProcessors));
		}
		return qa;		
	}


	/**
	 * main lineage query method. queries the provenance DB 
	 * with a single originating proc/var/path and a set of selected Processors
	 * @param wfID
	 * @param var
	 * @param proc
	 * @param path
	 * @param path2 
	 * @param selectedProcessors
	 * @return a list of bindings. each binding involves an input var for one of the selectedProcessors. Note 
	 * each var can contribute multiple bindings, i.e., when all elements in a collection bound to the var are retrieved.
	 * Note also that bindings for input vars are returned as well, when the query is configured with returnOutputs = true
	 * {@link ProvenanceAnalysis#isReturnOutputs() }
	 * @throws SQLException
	 */
	public List<Dependencies> computeLineageSingleBinding(
			String wfID,   // dynamic scope
			String wfNameRef,  // static scope
			String var,   // target var
			String proc,   // qualified with its processor name
			String path,   // possibly empty when no collections or no granular lineage required
			List<ProvenanceProcessor> selectedProcessors
	) throws SQLException  {

//		Map<String, LineageSQLQuery>  varName2lqList =  new HashMap<String, LineageSQLQuery>();

//		System.out.println("timing starts...");
		long start = System.currentTimeMillis();

		List<LineageSQLQuery>  lqList =  searchDataflowGraph(wfID, wfNameRef, var, proc, path, selectedProcessors);
		long stop = System.currentTimeMillis();

		long gst = stop-start;

		// execute queries in the LineageSQLQuery list
		logger.info("\n****************  executing lineage queries:  (includeDataValue is "+ isIncludeDataValue() +"**************\n");
		start = System.currentTimeMillis();

		List<Dependencies> results =  getPq().runLineageQueries(lqList, isIncludeDataValue());
		stop = System.currentTimeMillis();

		long qrt = stop-start;
		logger.info("search time: "+gst+"ms\nlineage query response time: "+qrt+" ms");
		logger.info("total exec time "+(gst+qrt)+"ms");

		return results;
	}


	/**
	 * compute lineage queries using path projections
	 * @param wfID the (single) instance defines the scope of a query<br/>
	 * added 2/9: collect a list of paths in the process to be used by the naive query. In practice
	 * we use this as the graph search phase that is needed by the naive query anyway
	 * @param var
	 * @param proc
	 * @param path  within var (can be empty but not null)
	 * @param selectedProcessors pairs (wfID, proceName), encoded as a Map. only report lineage when you reach any of these processors
	 * @throws SQLException
	 */
	public List<LineageSQLQuery> searchDataflowGraph(
			String wfID,   // dymamic scope
			String wfNameRef,  // static scope
			String var,   // target var
			String proc,   // qualified with its processor name
			String path,  // can be empty but not null
			List<ProvenanceProcessor> selectedProcessors
	) throws SQLException  {

		List<LineageSQLQuery>  lqList =  new ArrayList<LineageSQLQuery>();

		// TODO we are ignoring the wfId context information in the list of selected processors!!

		// init paths accumulation. here "path" is a path in the graph, not within a collection!
		//  associate an empty list of paths to each selected processor
		for (ProvenanceProcessor s:selectedProcessors) { validPaths.put(s, new ArrayList<List<String>>()); }

		currentPath = new ArrayList<String>();

		// start with xfer or xform depending on whether initial var is output or input

		// get (var, proc) from Port  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();
		varQueryConstraints.put("W.instanceID", wfID);
		varQueryConstraints.put("V.pnameRef", proc);  
		varQueryConstraints.put("V.varName", var);  
		varQueryConstraints.put("V.wfInstanceRef", wfNameRef);  

		List<Port> vars = getPq().getPorts(varQueryConstraints);

		if (vars.isEmpty())  {
			logger.info("variable ("+var+","+proc+") not found, lineage query terminated");
			return null;
		}

		Port v = vars.get(0); 		// expect exactly one record
		// CHECK there can be multiple (pname, varname) pairs, i.e., in case of nested workflows
		// here we pick the first that turns up -- we would need to let users choose, or process all of them...

		if (v.isInputPort() || getPq().isDataflow(proc)) { // if vName is input, then do a xfer() step

			// rec. accumulates SQL queries into lqList
			xferStep(wfID, wfNameRef, v, path, selectedProcessors, lqList);

		} else { // start with xform

			// rec. accumulates SQL queries into lqList
			xformStep(wfID, wfNameRef, v, proc, path, selectedProcessors, lqList);			
		}

		return lqList;

	}  // end searchDataflowGraph



	/**
	 * accounts for an inverse transformation from one output to all inputs of a processor
	 * @param wfID
	 * @param var  the output var
	 * @param proc  the processor
	 * @param selectedProcessors  the processors for which we are interested in producing lineage 
	 * @param path iteration vector within a PortBinding collection
	 * @param lqList  partial list of spot lineage queries, to be added to
	 * @throws SQLException 
	 */
	private void xformStep(
			String wfID,
			String wfNameRef, 				
			Port outputVar, // we need the dnl from this output var
			String proc,
			String path,
			List<ProvenanceProcessor> selectedProcessors, 
			List<LineageSQLQuery> lqList 
	) throws SQLException {

		// retrieve input vars for current processor 
		Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

		List<Port>  inputVars = null;

		// here we fetch the input vars for the current proc.
		// however, it may be the case that we are looking at a dataflow port (for the entire dataflow or
		// for a subdataflow) rather than a real processor. in this case 
		// we treat this as a 
		// special processor that does nothing -- so we "input var" in this case 
		// is a copy of the port, and we are ready to go for the next xfer step.
		// in this way we can seamlessly traverse the graph over intermediate I/O that are part 
		// of nested dataflows

		if (getPq().isDataflow(proc)) { // if we are looking at the output of an entire dataflow

			// force the "input vars" for this step to be the output var itself
			// this causes the following xfer step to trace back to the next processor _within_ proc 
			inputVars = new ArrayList<Port>();
			inputVars.add(outputVar);

		} else if (proc.equals(OUTPUT_CONTAINER_PROCESSOR)) {  // same action as prev case, but may change in the future

			inputVars = new ArrayList<Port>();
			inputVars.add(outputVar);

		} else {

			varsQueryConstraints.put("W.instanceID", wfID);
			varsQueryConstraints.put("pnameRef", proc);  
			varsQueryConstraints.put("inputOrOutput", "1");  

			inputVars = getPq().getPorts(varsQueryConstraints);
		}

		///////////
		/// path projections
		///////////
		// maps each var to its projected path
		Map<Port,String> var2Path = new HashMap<Port,String>();
		Map<Port,Integer> var2delta = new HashMap<Port,Integer>();

		if (path == null) {  // nothing to split
			for (Port inputVar: inputVars)  var2Path.put(inputVar, null);
		} else {

			int minPathLength = 0;  // if input path is shorter than this we give up granularity altogether
			for (Port inputVar: inputVars) {
				int delta = inputVar.getGranularDepth() - inputVar.getDepth();
				var2delta.put(inputVar, new Integer(delta));
				minPathLength += delta;
//				System.out.println("xform() from ["+proc+"] upwards to ["+inputVar.getPName()+":"+inputVar.getVName()+"]");
			}

			String iterationVector[] = path.split(",");

			if (iterationVector.length < minPathLength) {  // no path is propagated
				for (Port inputVar: inputVars) {
					var2Path.put(inputVar, null);
				}
			} else { // compute projected paths

				String[] projectedPath; 

				int start = 0;
				for (Port inputVar: inputVars) {

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

		// accumulate this proc to current path 
		currentPath.add(proc);

		// if this is a selected processor, add a copy of the current path to the list of paths for the processor

		// is <wfNameRef, proc>  in selectedProcessors?
		boolean isSelected = false;
		for (ProvenanceProcessor pp: selectedProcessors)  {
			if (pp.getWfInstanceRef().equals(wfNameRef) && pp.getPname().equals(proc)) {
				List<List<String>> paths = validPaths.get(pp);

				// copy the path since the original will change
				// also remove spurious dataflow processors at this point
				List<String> pathCopy = new ArrayList<String>();
				for (String s:currentPath) {
					if (!getPq().isDataflow(s)) pathCopy.add(s);
				}			
				paths.add(pathCopy);
				isSelected = true;
				break;
			}
		}

		///////////
		/// generate SQL if necessary -- for all input vars, based on the current path
		/// the projected paths are required to determine the level in the collection at which 
		/// we look at the value assignment
		///////////

		Map<String, ProvenanceArtifact> var2Artifact = new HashMap<String, ProvenanceArtifact>();
		Map<String, ProvenanceRole> var2ArtifactRole = new HashMap<String, ProvenanceRole>();

		// if this transformation is important to the user, produce an output and also an OPM graph fragment
		if (selectedProcessors.isEmpty() || isSelected) {

			List<LineageSQLQuery> newLqList = getPq().lineageQueryGen(wfID, proc, var2Path, outputVar, path, isReturnOutputs() || var2Path.isEmpty());
			lqList.addAll(newLqList);

			// BEGIN OPM update section
			//
			// create OPM artifact and role for the output var of this xform
			//
			boolean doOPM = (aOPMManager != null && aOPMManager.isActive());  // any problem below will set this to false

			String role = null;
			PortBinding vb = null;
			String URIFriendlyIterationVector =null;

			if (doOPM) {
				// fetch value for this variable and assert it as an Artifact in the OPM graph
				Map<String, String> vbConstraints = new HashMap<String, String>();
				vbConstraints.put("VB.PNameRef", outputVar.getProcessorName());
				vbConstraints.put("VB.varNameRef", outputVar.getPortName());
				vbConstraints.put("VB.wfInstanceRef", wfID);

				if (path != null) { 

					// account for x,y,.. format as well as [x,y,...]  depending on where the request is coming from
					// TODO this is just irritating must be removed
					if (path.startsWith("[")) 
						vbConstraints.put("VB.iteration", path);
					else
						vbConstraints.put("VB.iteration", "["+path+"]");
				}

				List<PortBinding> vbList = getPq().getPortBindings(vbConstraints); // DB

				// use only the first result (expect only one) -- in this method we assume path is not null

				// map the resulting varBinding to an Artifact
				if (vbList == null || vbList.size()==0) {
					logger.debug("no entry corresponding to conditions: proc="+
							outputVar.getProcessorName()+" var = "+outputVar.getPortName()+" iteration = "+path);
					doOPM = false;
				}  else {
					vb = vbList.get(0);

					URIFriendlyIterationVector = vb.getIteration().
					replace(',', '-').replace('[', ' ').replace(']', ' ').trim();

					if (URIFriendlyIterationVector.length()>0) {
						role = vb.getPNameRef()+"/"+vb.getVarNameRef()+"?it="+URIFriendlyIterationVector;
					} else
						role = vb.getPNameRef()+"/"+vb.getVarNameRef();

					if (aOPMManager!=null && !pq.isDataflow(proc)) {
						if (isRecordArtifactValues())
							aOPMManager.addArtifact(vb.getValue(), vb.getResolvedValue());
						else 
							aOPMManager.addArtifact(vb.getValue());

						aOPMManager.createRole(role);
					}

					// assert proc as Process -- include iteration vector to separate different activations of the same process					
					aOPMManager.addProcess(proc, vb.getIteration(), URIFriendlyIterationVector);

					//
					// create OPM generatedBy property between output value and this process node
					// avoid the pathological case where a dataflow generates its own inputs
					//
					aOPMManager.assertGeneratedBy(
							aOPMManager.getCurrentArtifact(), 
							aOPMManager.getCurrentProcess(), 
							aOPMManager.getCurrentRole(), 
							aOPMManager.getCurrentAccount(),
							true);   // true -> prevent duplicates CHECK
				}
			}
			// 
			// create OPM process for this xform
			//
			for (LineageSQLQuery lq: newLqList) {
				// if OPM is on, execute the query so we get the value we need for the Artifact node
				Dependencies inputs = getPq().runLineageQuery(lq, isIncludeDataValue());

				if (doOPM && inputs.getRecords().size()>0 && !pq.isDataflow(proc)) {

					//	update OPM graph with inputs and used properties
					for (LineageQueryResultRecord resultRecord: inputs.getRecords()) {

						// process inputs only
						if (!resultRecord.isInput()) continue;

						URIFriendlyIterationVector = resultRecord.getIteration().
						replace(',', '-').replace('[', ' ').replace(']', ' ').trim();

						boolean found = false;  // used to avoid duplicate process resources

						// map each input var in the resultRecord to an Artifact
						// create new Resource for the resultRecord
						//    use the value as URI for the Artifact, and resolvedValue as the actual value

						//
						// create OPM artifact and role for the input var obtained by path projection
						//
						if (isRecordArtifactValues())							
							aOPMManager.addArtifact(resultRecord.getValue(), resultRecord.getResolvedValue());
						else 
							aOPMManager.addArtifact(resultRecord.getValue());
						var2Artifact.put(resultRecord.getVname(), aOPMManager.getCurrentArtifact());

						if (URIFriendlyIterationVector.length()>0) {
							role = resultRecord.getPname()+"/"+resultRecord.getVname()+"?it="+URIFriendlyIterationVector;
						} else
							role = resultRecord.getPname()+"/"+resultRecord.getVname();

						aOPMManager.createRole(role);	// this also sets currentRole to role				
						var2ArtifactRole.put(resultRecord.getVname(), aOPMManager.getCurrentRole());


						//
						// create OPM used property between process and the input var obtained by path projection
						//
						// avoid output variables, it would assert that P used one of its outputs!

						aOPMManager.assertUsed(
								aOPMManager.getCurrentArtifact(), 
								aOPMManager.getCurrentProcess(), 
								aOPMManager.getCurrentRole(), 
								aOPMManager.getCurrentAccount(),
								true);   // true -> prevent duplicates CHECK	
					}
				}
			}
//			END OPM update section
		}

		// recursion -- xfer path is next up
		for (Port inputVar: inputVars) {
			xferStep(wfID, wfNameRef, inputVar, var2Path.get(inputVar), selectedProcessors, lqList);	
		}
		currentPath.remove(currentPath.size()-1);  // CHECK	
	}  // end xformStep



	private void xferStep(
			String wfInstanceID,
			String wfNameRef, 
			Port port,
			String path, 
			List<ProvenanceProcessor> selectedProcessors,
			List<LineageSQLQuery> lqList) throws SQLException {

		String sourceProcName = null;
		String sourceVarName  = null;

		// retrieve all Datalinks ending with (var,proc) -- ideally there is exactly one
		// (because multiple incoming datalinks are disallowed)
		Map<String, String>  datalinksQueryConstraints = new HashMap<String, String>();

		datalinksQueryConstraints.put("W.instanceID", wfInstanceID);
		datalinksQueryConstraints.put("destinationPortId", port.getIdentifier());
		List<DataLink> datalinks = getPq().getDataLinks(datalinksQueryConstraints);

		if (datalinks.size() == 0) {
//			System.out.println("no datalinks going up from ["+proc+","+var+"] ... returning");
			return; // CHECK
		}

		DataLink a = datalinks.get(0); 

		// get source node
		sourceProcName = a.getSourceProcessorName();
		sourceVarName  = a.getSourcePortName();

		//System.out.println("xfer() from ["+proc+","+var+"] to ["+sourceProcName+","+sourceVarName+"]");

		// CHECK transfer same path with only exception: when anl(sink) > anl(source)
		// in this case set path to null

		// retrieve full record for var:
		// retrieve input vars for current processor 
		Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

//		varsQueryConstraints.put("W.instanceID", wfInstanceID);
		varsQueryConstraints.put("portId", a.getSourcePortId());
//		varsQueryConstraints.put("pnameRef", sourceProcName);  
//		varsQueryConstraints.put("varName", sourceVarName);  
		List<Port>  varList  = getPq().getPorts(varsQueryConstraints);

		Port outputVar = varList.get(0);

		// recurse on xform
		xformStep(wfInstanceID, wfNameRef, outputVar, sourceProcName, path, selectedProcessors, lqList);

	} // end xferStep2


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
		int ANL  = 0;  // actual nesting level -- copied from Port

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


	/**
	 * @return the validPaths
	 */
	public Map<ProvenanceProcessor, List<List<String>>> getValidPaths() {
		return validPaths;
	}


	/**
	 * @param validPaths the validPaths to set
	 */
	public void setValidPaths(Map<ProvenanceProcessor, List<List<String>>> validPaths) {
		this.validPaths = validPaths;
	}


	public void setPq(ProvenanceQuery pq) {
		this.pq = pq;
	}


	public ProvenanceQuery getPq() {
		return pq;
	}

	/**
	 * @return the ready
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * @param ready the ready to set
	 */
	public void setReady(boolean ready) {
		this.ready = ready;
	}

	/**
	 * @return the returnOutputs
	 */
	public boolean isReturnOutputs() {
		return returnOutputs;
	}

	/**
	 * @param returnOutputs the returnOutputs to set
	 */
	public void setReturnOutputs(boolean returnOutputs) {
		this.returnOutputs = returnOutputs;
	}

	/**
	 * @return the recordArtifactValues
	 */
	public boolean isRecordArtifactValues() {
		return recordArtifactValues;
	}

	/**
	 * @param recordArtifactValues the recordArtifactValues to set
	 */
	public void setRecordArtifactValues(boolean recordArtifactValues) {
		this.recordArtifactValues = recordArtifactValues;

	}

	/**
	 * @return the includeDataValue
	 */
	public boolean isIncludeDataValue() {
		return includeDataValue;
	}

	/**
	 * @param includeDataValue the includeDataValue to set
	 */
	public void setIncludeDataValue(boolean includeDataValue) {
		this.includeDataValue = includeDataValue;
	}

	/**
	 * @return the generateOPMGraph
	 */
	public boolean isGenerateOPMGraph() {
		return generateOPMGraph;
	}

	/**
	 * @param generateOPMGraph the generateOPMGraph to set
	 */
	public void setGenerateOPMGraph(boolean generateOPMGraph) {
		this.generateOPMGraph = generateOPMGraph;
		if (aOPMManager != null) { aOPMManager.setActive(generateOPMGraph); }
	}


}
