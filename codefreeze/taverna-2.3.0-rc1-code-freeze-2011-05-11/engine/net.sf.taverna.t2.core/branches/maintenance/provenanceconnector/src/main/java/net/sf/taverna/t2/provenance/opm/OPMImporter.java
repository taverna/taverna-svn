/**
 * 
 */
package net.sf.taverna.t2.provenance.opm;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.PortBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;

import org.apache.log4j.Logger;
import org.openprovenance.model.Account;
import org.openprovenance.model.AccountId;
import org.openprovenance.model.Accounts;
import org.openprovenance.model.Artifact;
import org.openprovenance.model.ArtifactId;
import org.openprovenance.model.Artifacts;
import org.openprovenance.model.CausalDependencies;
import org.openprovenance.model.OPMDeserialiser;
import org.openprovenance.model.OPMGraph;
import org.openprovenance.model.ProcessId;
import org.openprovenance.model.Role;
import org.openprovenance.model.Used;
import org.openprovenance.model.WasControlledBy;
import org.openprovenance.model.WasDerivedFrom;
import org.openprovenance.model.WasGeneratedBy;
import org.openprovenance.model.WasTriggeredBy;

/**
 * @author paolo
 * imports foreign XML-serialized OPM graphs into the native Taverna provenance DB, so they can be queried using
 * 
 * {@link net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis} 
 */
public class OPMImporter {

	private static final String PROC_NAME = "P";
	private static final String OPM_DEF_ACCOUNT = "OPMDefaultAccount";
	ProvenanceWriter pw;
	OPMGraph graph;

	private static Logger logger = Logger.getLogger(OPMImporter.class);

	// Maps Account names to Taverna workflows
	Map<String, String> accountToWorkflow  = new HashMap<String, String>();
	Map<String, String> workflowToInstance = new HashMap<String, String>();

	// maps workflowId --> (workflowId --> List(Port))
	private Map<String, Map<String, List<Port>>> usedVarsByAccount = new HashMap<String, Map<String, List<Port>>>();
	private Map<String, Map<String, List<Port>>> wgbVarsByAccount = new HashMap<String, Map<String, List<Port>>>();

	// maps accountname --> (artifact -> List(Process))
	private Map<String, Map<String,List<String>>> wgbArtifactsByAccount = new HashMap<String, Map<String,List<String>>>(); 

	// maps accountname --> (artifact -> List(Process))
	private Map<String, Map<String,List<String>>> usedArtifactsByAccount = new HashMap<String, Map<String,List<String>>>(); 

	int procNameCounter;
	private String masterAccount = OPM_DEF_ACCOUNT;

	public OPMImporter(ProvenanceWriter pw) {
		this.pw = pw;
	}	

	/**
	 * orphan artifacts are those that are in the graph but are never used neither generated. this 
	 * indicates some problem with the graph structure. this method is used for diagnostics after import has finished
	 * @return
	 */
	public List<String> getOrphanArtifacts() {

		List<String> allwgb  = new ArrayList<String>();
		List<String> allUsed = new ArrayList<String>();
		List<String> orphans = new ArrayList<String>();

		if (graph == null)  {
			logger.warn("null graph while attempting to count orphan artifacts -- giving up");
			return orphans; 
		}
		
		Artifacts allArtifacts = graph.getArtifacts();

		for ( Map.Entry<String, Map<String,List<String>>>entry: wgbArtifactsByAccount.entrySet()) {
			allwgb.addAll(entry.getValue().keySet());
		}

		for ( Map.Entry<String, Map<String,List<String>>>entry: usedArtifactsByAccount.entrySet()) {
			allUsed.addAll(entry.getValue().keySet());
		}

		List<Artifact> artifacts = allArtifacts.getArtifact();

		for (Artifact a:artifacts) {
			if (!allwgb.contains(a.getId()) && !allUsed.contains(a.getId())) {
				orphans.add(a.getId());
			}
		}
		return orphans;
	}


	public void importGraph(String XMLOPMGraphFilename) throws Exception, SQLException  {

		try {
			logger.info("Importing OPM XML from file "+XMLOPMGraphFilename);

			// deserialize an XML OPM graph from file
			OPMDeserialiser deser = new OPMDeserialiser();
			graph = deser.deserialiseOPMGraph(new File(XMLOPMGraphFilename));

		} catch (Exception e) {
			logger.fatal("exception while deserializing -- unable to continue");
			logger.fatal(e.getMessage());
			return;
		}

		logger.debug("XML graph deserialized");

		// 
		// generates one pair <workflowId, workflowRun> for each account in the graph
		//
		List<Account> allAccounts = null;
		try {
			Accounts accounts = graph.getAccounts();

			// use this global account alongside any other that may be defined in the graph
			generateWFFromAccount(OPM_DEF_ACCOUNT);

			if (accounts == null) {
				logger.warn("this graph contains no accounts -- using only the default");
			} else {
				for (Account acc:accounts.getAccount()) {
					// writes both workflow and instance into the DB, updates accountToWorkflow
					generateWFFromAccount(acc.getId());   
				}
			}
		} catch (Exception e) {
			logger.warn("exception while getting accounts for this graph");
		}

		// 
		// associates processes and ports to workflows and varbindings to corresponding workflowRuns
		//
		List<Object> allDeps;

		// what have we got?
		// retrieve all OPM relations from the graph		
		CausalDependencies cd = graph.getCausalDependencies();
		allDeps = cd.getUsedOrWasGeneratedByOrWasTriggeredBy();

		// make sure these are processed in the right order: used, wgby, THEN wdf because this latter is derived from the first 2!
		// so collect them into sets and process them separately

		Set<WasGeneratedBy> wgbSet = new HashSet<WasGeneratedBy>();
		Set<Used> usedSet = new HashSet<Used>();
		Set<WasDerivedFrom> wdfSet = new HashSet<WasDerivedFrom>();
		Set<WasControlledBy> wcbSet = new HashSet<WasControlledBy>();
		Set<WasTriggeredBy> wtbSet = new HashSet<WasTriggeredBy>();

		for (Object dep:allDeps) {
			logger.info("dependency of type: "+dep.getClass().getName());

			if (dep instanceof org.openprovenance.model.WasGeneratedBy) {
				wgbSet.add((WasGeneratedBy) dep);
			} else if (dep instanceof org.openprovenance.model.Used) {
				usedSet.add((Used) dep);
			} else if (dep instanceof org.openprovenance.model.WasDerivedFrom) {
				wdfSet.add((WasDerivedFrom) dep);
			} else if (dep instanceof org.openprovenance.model.WasControlledBy) {
				wcbSet.add((WasControlledBy) dep);
			} else if (dep instanceof org.openprovenance.model.WasTriggeredBy) {
				wtbSet.add((WasTriggeredBy) dep);
			}
		}

		// process these in the correct order
		int cnt =0;  // used to debug a nasty outofmemory error
		for (WasGeneratedBy dep: wgbSet) {
//			logger.debug(cnt++);
			processWGBy(dep);
		}

		for (Used dep:usedSet) processUsed(dep);

		for (WasDerivedFrom dep: wdfSet) processWDF(dep);

		// we actually ignore the others... 

		// *********
		// complete the induced graph by building datalinks using the Artifact -> [Port] maps
		// *********

		List<String>  accountNames = new ArrayList<String>();

		accountNames.add(OPM_DEF_ACCOUNT);

		if (allAccounts != null)  
			for (Account acc:allAccounts) { accountNames.add(acc.getId()); }

		for (String acc:accountNames) {

			String workflowId = accountToWorkflow.get(acc);

			Map<String, List<Port>> usedVars = usedVarsByAccount.get(workflowId);
			Map<String, List<Port>> wgbVars =  wgbVarsByAccount.get(workflowId);

			if (usedVars == null || wgbVars == null) continue;

			// install an Datalink from each wgb var to each used var when the artifact is the same
			for (Map.Entry<String, List<Port>> entry:wgbVars.entrySet()) {

				// all Ports for this artifact get connected to all corresponding Ports in used
				List<Port> sourceVars = entry.getValue();				
				List<Port> targetVars = usedVars.get(entry.getKey());

				if (sourceVars == null || targetVars == null) continue;

				// create an datalink from each sourceVar to each targetVar
				// note that we expect a single targetVar, but this is not guaranteed
				for (Port sourceVar:sourceVars) {
					for (Port targetVar:targetVars) {
						pw.addDataLink(sourceVar, targetVar, workflowId);
					}
				}
			}
		}
	}

	private void generateWFFromAccount(String accName) throws SQLException {

		String workflowId     = accName+"-"+UUID.randomUUID().toString();
		String workflowRun = accName+"-"+UUID.randomUUID().toString();

		pw.addWFId(workflowId);
		pw.addWorkflowRun(workflowId, workflowRun);
		accountToWorkflow.put(accName, workflowId);
		workflowToInstance.put(workflowId, workflowRun);

		logger.info("generated workflowId "+workflowId+" and instance "+workflowRun+"  for account "+accName);
	}


	private Port processProcessArtifactDep(String procName, String value, String portName,
			String workflowId, String workflowRun, boolean artifactIsInput) {

		// generate Process
		ProvenanceProcessor proc = null;
		try {
			proc = pw.addProcessor(procName, workflowId, false);
			logger.debug("added processor "+procName+" to workflow "+workflowId);
		} catch (SQLException e) {  // no panic -- just catch duplicates
			logger.warn(e.getMessage());
		}

		// generate Port
		Port outputVar = new Port();
		outputVar.setProcessorId(proc.getIdentifier());
		outputVar.setProcessorName(procName);
		outputVar.setWorkflowId(workflowId);
		outputVar.setPortName(portName);
		outputVar.setDepth(0);
		outputVar.setInputPort(artifactIsInput);  // wgby is an output var   

		List<Port> vars = new ArrayList<Port>(); // only one Port in the list
		vars.add(outputVar);

		try {
			pw.addPorts(vars, workflowId);
			logger.debug("added var "+portName+" to workflow "+workflowId);
		} catch (SQLException e) {  // no panic -- just catch duplicates
			logger.warn(e.getMessage());
		}

		// generate PortBindings (workflowRun, procName, portName, value)			
		PortBinding vb = new PortBinding();

		vb.setWorkflowRunId(workflowRun);
		vb.setProcessorName(procName);
		vb.setPortName(portName);
		vb.setValue(value);
		vb.setIteration("[]");

		try {
			pw.addPortBinding(vb);
			logger.debug("added var binding with value "+value+" to workflow instance "+workflowRun);
		} catch (SQLException e) {  // no panic -- just catch duplicates
			logger.error("Failed to add var binding: " + e.getMessage());
		}

		return outputVar;
	}


	/**
	 * generic processing of a process-artifact dependency
	 * @param procID
	 * @param artId
	 * @param role
	 * @param workflowId
	 * @param workflowRun
	 * @param artifactIsInput
	 */
	private Port processProcessArtifactDep(ProcessId procID, ArtifactId artId, Role role, 
			String workflowId, String workflowRun, boolean artifactIsInput) {

		String procName = ((org.openprovenance.model.Process) procID.getId()).getId();
		String portName  = role.getValue();
		String value    = ((Artifact) artId.getId()).getId();

		portName = removeBlanks(portName);

		return processProcessArtifactDep(procName, value, portName, workflowId, workflowRun, artifactIsInput);
	}



	private String removeBlanks(String portName) {		
		return portName.replace(" ", "_");
	}


	/**
	 * used(A,R,P,acc): generates a process for P, a Port for (P,R) an <em>input</em> PortBinding for (P,R,A)
	 * <br/> this is very similar to {@link #processWGBy(WasGeneratedBy)}
	 * @param dep
	 */
	private void processUsed(Used dep) {

		// Acc determines the scope -- this dep may belong to > 1 account, deal with all of them
		List<AccountId> accountIDs = dep.getAccount();
		ProcessId procID = dep.getEffect();
		ArtifactId artId = dep.getCause();
		Role role = dep.getRole();

		List<String>  accNames = new ArrayList<String>();

		for (AccountId accId:accountIDs) {
			accNames.add(((Account) accId.getId()).getId());
		}

		accNames.add(OPM_DEF_ACCOUNT);

		for (String accName: accNames) {
			String workflowId = accountToWorkflow.get(accName);
			String workflowRun = workflowToInstance.get(workflowId);

			Port v  = processProcessArtifactDep(procID, artId, role, workflowId, workflowRun, true);  // true -> input var

			// save the mapping from artifact to var for this account
			Map<String, List<Port>> usedVars = usedVarsByAccount.get(workflowId);
			if (usedVars == null) {
				usedVars = new HashMap<String, List<Port>>();
				usedVarsByAccount.put(workflowId, usedVars);
			}
			List<Port> vars = usedVars.get(((Artifact) artId.getId()).getId());

			if (vars == null) {
				vars = new ArrayList<Port>();
				usedVars.put(((Artifact) artId.getId()).getId(), vars);
			}
			vars.add(v);

			// record the fact that (procID used artId) within this account
			Map<String, List<String>> usedArtifacts = usedArtifactsByAccount.get(accName);
			if (usedArtifacts == null) {
				usedArtifacts = new HashMap<String, List<String>>();
				usedArtifactsByAccount.put(accName, usedArtifacts);
			}

			String artifactName = ((Artifact) artId.getId()).getId();
			List<String> processes = usedArtifacts.get(artifactName);
			if (processes == null) {
				processes = new ArrayList<String>();
				usedArtifacts.put(artifactName, processes);
			}
			processes.add(((org.openprovenance.model.Process) procID.getId()).getId());
		}
	}



	/**
	 * wgb(A,R,P,Acc): generates a Process for P, a Port for (P,R), an <em>output</em> PortBinding for (P,R,A) 
	 * This is all relative to the workflow corresponding to account Acc. <br/>
	 * 
	 * @param dep 
	 * @throws SQLException 
	 */
	private void processWGBy(WasGeneratedBy dep)  {

		// Acc determines the scope -- this dep may belong to > 1 account, deal with all of them
		List<AccountId> accountIDs = dep.getAccount();
		ProcessId procID = dep.getCause();
		ArtifactId artId = dep.getEffect();
		Role role = dep.getRole();

		List<String>  accNames = new ArrayList<String>();

		for (AccountId accId:accountIDs) {
			accNames.add(((Account) accId.getId()).getId());
		}

		accNames.add(OPM_DEF_ACCOUNT);

		for (String accName:accNames) {

			String workflowId = accountToWorkflow.get(accName);
			String workflowRun = workflowToInstance.get(workflowId);

			Port v = processProcessArtifactDep(procID, artId, role, workflowId, workflowRun, false);  // false -> output var

			Map<String, List<Port>> wgbVars = wgbVarsByAccount.get(workflowId);
			if (wgbVars == null) {
				wgbVars = new HashMap<String, List<Port>>();
				wgbVarsByAccount.put(workflowId, wgbVars);
			}

			List<Port> vars = wgbVars.get(((Artifact) artId.getId()).getId());
			if (vars == null) {
				vars = new ArrayList<Port>();
				wgbVars.put(((Artifact) artId.getId()).getId(), vars);
			}
			vars.add(v);

			// record the fact that (artId wgby procID) within this account
			Map<String, List<String>> wgbArtifacts = wgbArtifactsByAccount.get(accName);
			if (wgbArtifacts == null) {
				wgbArtifacts = new HashMap<String, List<String>>();
				wgbArtifactsByAccount.put(accName, wgbArtifacts);
			}

			String artifactName = ((Artifact) artId.getId()).getId();
			List<String> processes = wgbArtifacts.get(artifactName);
			if (processes == null) {
				processes = new ArrayList<String>();
				wgbArtifacts.put(artifactName, processes);
			}
			processes.add(((org.openprovenance.model.Process) procID.getId()).getId());
		}
	}


	/**
	 * this is a dep between two artifacts A1 and A2.
	 * In Taverna we need to postulate the existence of a Process to mediate this dependency.
	 * <br/> However, we only need to account for this dep if it cannot be inferred from a combination of used and wgby that 
	 * involve A1 and A2:  if there exists P s.t. A1 wgby P and P used A2, then this dep. is redundant in the DB and we can safely ignore it.
	 * <br/> note that this analysis is conducted regardless of the accounts in which the wgby and used properties appear, as one account could 
	 * be used deliberately to 
	 * This will unclutter the DB.
	 * @param dep
	 */
	private void processWDF(WasDerivedFrom dep) {
		List<AccountId> accountIDs = dep.getAccount();
		ArtifactId fromArtId = dep.getCause();
		ArtifactId toArtId = dep.getEffect();

		List<String>  accNames = new ArrayList<String>();

		for (AccountId accId:accountIDs) {
			accNames.add(((Account) accId.getId()).getId());
		}

		accNames.add(OPM_DEF_ACCOUNT);

		for (String accName:accNames) {

			int varCounter = 0;

			String workflowId = accountToWorkflow.get(accName);
			String workflowRun = workflowToInstance.get(workflowId);

			List<String> generatingProcesses=null, usingProcesses=null;

			// look for any triple fromArtId wasGeneratedBy P within this account
			Map<String, List<String>> wgbArtifacts = wgbArtifactsByAccount.get(accName);

			if (wgbArtifacts != null) {
				String toArtifactName = ((Artifact) toArtId.getId()).getId();
				generatingProcesses = wgbArtifacts.get(toArtifactName);
				if (generatingProcesses != null) {
					logger.debug("artifact "+toArtifactName+" wgby one or more processes...");
				}
			}

			// look for any triple (P used toArtId) within this account

			// get map for this account
			Map<String, List<String>> usedArtifacts = usedArtifactsByAccount.get(accName);

			if (usedArtifacts != null) {
				String fromArtifactName = ((Artifact) fromArtId.getId()).getId();
				usingProcesses = usedArtifacts.get(fromArtifactName);
				if (usingProcesses != null) {
					logger.debug("artifact "+fromArtifactName+" was used by one or more processes...");
				}
			}

			boolean found = false;
			if (generatingProcesses != null && usingProcesses != null) {
				for (String gp:generatingProcesses) {
					if (usingProcesses.contains(gp)) { 
						logger.debug("intersection between process sets not empty, this WDF is redundant");
						found = true; 
						break; 
					} 
				}
			}

			// only postulate a new process if the native one has not been found
			if (found) return;

			String procName = PROC_NAME+"_"+procNameCounter++;

			try {
				pw.addProcessor(procName, workflowId, false);
				logger.info("created non-native added processor "+procName+" to workflow "+workflowId);
			} catch (SQLException e) {  // no panic -- just catch duplicates
				logger.warn(e.getMessage());
			}

			// create a role for fromArtId from the procName
			String inputPortName = procName+"_"+varCounter++;
			String inputValue = ((Artifact) fromArtId.getId()).getId();

			// add to DB
			processProcessArtifactDep(procName, inputValue, inputPortName, workflowId, workflowRun, true);

			// create a role for toArtId
			String outputPortName = procName+"_"+varCounter++;
			String outputValue = ((Artifact) toArtId.getId()).getId();

			// add to DB
			processProcessArtifactDep(procName, outputValue, outputPortName, workflowId, workflowRun, false);
		}		
	}



	/**
	 * there is no counterpart in Taverna provenance for this dependency. This is translated into
	 * a control link but this is not part of the provenance model
	 * @param dep
	 */
	private void processWTBy(WasTriggeredBy dep) {

	}


	/**
	 * there is no counterpart in Taverna for this dependency, as it involves agents which we don't support
	 * @param dep
	 */
	private void processWCBy(WasControlledBy dep) {
		// TODO Auto-generated method stub
	}



}
