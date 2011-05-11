/**
 * 
 */
package net.sf.taverna.t2.provenance.opm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.lineageservice.utils.DataValueExtractor;

import org.apache.log4j.Logger;
import org.openprovenance.model.Artifact;
import org.openprovenance.model.OPMGraph;
import org.openprovenance.model.OPMToDot;
import org.openprovenance.model.Process;
import org.openprovenance.rdf.OPMRdf2Xml;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.UnionContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.kernel.impl.ResourceContext;
import org.tupeloproject.provenance.ProvenanceAccount;
import org.tupeloproject.provenance.ProvenanceArtifact;
import org.tupeloproject.provenance.ProvenanceException;
import org.tupeloproject.provenance.ProvenanceGeneratedArc;
import org.tupeloproject.provenance.ProvenanceProcess;
import org.tupeloproject.provenance.ProvenanceRole;
import org.tupeloproject.provenance.ProvenanceUsedArc;
import org.tupeloproject.provenance.impl.ProvenanceContextFacade;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.xml.RdfXmlWriter;


/**
 * @author paolo
 *
 */
public class OPMManager {

	private static Logger logger = Logger.getLogger(OPMManager.class);

	public static final String OPM_TAVERNA_NAMESPACE = "http://taverna.opm.org/";
	private static final String OPM_RDF_GRAPH_FILE = "src/test/resources/provenance-testing/OPM/OPMGraph.rdf";
	private static final String  OPM_DOT_FILE = "src/test/resources/provenance-testing/OPM/OPMGraph.dot";
	private static final String VALUE_PROP = "value";
	
	ProvenanceContextFacade graph = null;
	Context context = null;

	ProvenanceAccount  currentAccount = null;
	ProvenanceArtifact currentArtifact = null;
	ProvenanceRole     currentRole = null;
	ProvenanceProcess  currentProcess = null;

	private boolean isActive = true;

	public OPMManager() {

		// init Tupelo RDF provenance graph
		MemoryContext mc = new MemoryContext();
		ResourceContext rc = new ResourceContext("http://example.org/data/","/provenanceExample/");
		context = new UnionContext();
		context.addChild(mc);
		context.addChild(rc);

		graph = new ProvenanceContextFacade(mc);
	}


	/**
	 * default implementation of this method returns null -- has no idea how to extract simple values from incoming artifact values 
	 * @return
	 */
	public List<DataValueExtractor> getDataValueExtractor() { return null; }


	/**
	 * 	create new account to hold the causality graph
	 *  and give it a Resource name
	 * @param accountName
	 * @throws ProvenanceException 
	 */
	public void createAccount(String accountName) throws ProvenanceException {

		currentAccount = graph.newAccount("OPM-"+
				accountName, Resource.uriRef(OPM_TAVERNA_NAMESPACE+accountName));
		graph.assertAccount(currentAccount);
	}


	/**
	 * 
	 * @param aName
	 * @param aValue  actual value can be used optionally as part of a separate triple. Whether this is used or not 
	 * depends on the settings, see {@link OPMManager.addValueTriple}.
	 * This also sets the currentArtifact to the newly created artifact
	 * @throws ProvenanceException 
	 */
	public void addArtifact(String aName, Object aValue) throws ProvenanceException {

		String artID=aName;
		// make sure artifact name is a good URI
		try {
			URI artURI = new URI(aName);

			if (artURI.getAuthority() == null) {
				artID = OPM_TAVERNA_NAMESPACE+aName;				
			}
		} catch (URISyntaxException e1) {
			artID = OPM_TAVERNA_NAMESPACE+aName;
		}


		Resource r = Resource.uriRef(artID);
		currentArtifact = graph.newArtifact(artID, r);
		graph.assertArtifact(currentArtifact);

		if (aValue != null) {
			logger.debug("OPMManager::addArtifact: aValue is NOT NULL");

			// if we have a valid DataValueExtractor, use it here
			List<DataValueExtractor> dveList;
			String extractedValue = (String) aValue;  // default is same value
			if ((dveList = getDataValueExtractor()) != null) {

				// try all available extractors... UGLY but data comes with NO TYPE at all!
				for (DataValueExtractor dve: dveList) {
					try {

						logger.debug("OPMManager::addArtifact: trying extractor "+dve.getClass().getName());
						extractedValue = dve.extractString(aValue);						
						logger.debug("OPMManager::addArtifact: - extracted value = "+extractedValue);
						break; // extractor worked
					} catch (Exception e) {
						// no panic, reset value and try another extractor
						logger.warn("OPMManager::addArtifact: extractor failed");
						extractedValue = (String) aValue;
					}
				}
			}

			logger.debug("OPMManager::addArtifact: using value "+extractedValue);
			try {
				Literal lValue = Resource.literal(extractedValue);
				context.addTriple(r, Resource.uriRef(OPM_TAVERNA_NAMESPACE+VALUE_PROP), lValue);
			} catch (OperatorException e) {
				logger.warn("OPM iteration triple creation exception", e);
			}
		}  else {
			logger.debug("OPMManager::addArtifact: aValue for ["+aName+"] is NULL");
		}
	}


	/**
	 * no actual value is recorded
	 * @param aName
	 * @throws ProvenanceException 
	 */
	public void addArtifact(String aName) throws ProvenanceException {

		Resource r = Resource.uriRef(aName);
		currentArtifact = graph.newArtifact(aName, r);
		graph.assertArtifact(currentArtifact);		
	}



	public void createRole(String aRole) {

		Resource r = Resource.uriRef(OPM_TAVERNA_NAMESPACE+aRole);		
		currentRole = graph.newRole(aRole, r);
	}


	public void addProcess(String proc, String iterationVector, String URIfriendlyIterationVector) throws ProvenanceException {

		String processID;

		// PM added 5/09 -- a process name may already be a URI -- this happens for example when we export back OPM
		// after importing a workflow from our own OPM... in this case, do not pre-pend a new URI scheme

		try {
			URI procURI = new URI(proc);

			if (procURI.getAuthority() == null) {
				processID = OPM_TAVERNA_NAMESPACE+proc;				
			} else {
				processID = proc;
			}
		} catch (URISyntaxException e1) {
			processID = OPM_TAVERNA_NAMESPACE+proc;
		}
		if (URIfriendlyIterationVector.length()>0) {
			processID = processID+"?it="+URIfriendlyIterationVector;
		}

		Resource processResource = Resource.uriRef(processID);					
		currentProcess = graph.newProcess(processID, processResource);
		graph.assertProcess(currentProcess );

		// add a triple to specify the iteration vector for this occurrence of Process, if it is available
		if (URIfriendlyIterationVector.length() > 0) {
//			Resource inputProcessSubject = ((RdfProvenanceProcess) process).getSubject();
			try {
				context.addTriple(processResource, Resource.uriRef(OPM_TAVERNA_NAMESPACE+"iteration"), iterationVector);
			} catch (OperatorException e) {
				logger.warn("OPM iteration triple creation exception", e);
			}
		}
	}


	public void assertGeneratedBy(ProvenanceArtifact artifact, 
			ProvenanceProcess process, 
			ProvenanceRole role, 
			ProvenanceAccount account,
			boolean noDuplicates) throws ProvenanceException {

		boolean found = false;
		if (noDuplicates && artifact != null) {
			Collection<ProvenanceGeneratedArc> generatedBy = graph.getGeneratedBy(artifact);

			for (ProvenanceGeneratedArc datalink:generatedBy) {						
				ProvenanceProcess pp = datalink.getProcess();
				if (pp.getName().equals(process.getName())) { found = true; break; }						
			}
		}

		if (!noDuplicates || (noDuplicates && !found) && artifact != null)
			graph.assertGeneratedBy(artifact, process, role, account);
	}



	public void assertUsed(ProvenanceArtifact artifact,			
			ProvenanceProcess process, 
			ProvenanceRole role,
			ProvenanceAccount account, 
			boolean noDuplicates) throws ProvenanceException {

		boolean found = false;

//		logger.debug("assertUsed: for process: "+process.getName()+"  and role "+role.getName());
		
		if (noDuplicates) {
			Collection<ProvenanceUsedArc> used = graph.getUsed(process);

			for (ProvenanceUsedArc datalink:used) {						
				ProvenanceArtifact pa = datalink.getArtifact();
				if (pa.getName().equals(artifact.getName())) { found = true; break; }						
			}
		}

		if (!noDuplicates || (noDuplicates && !found) )
			graph.assertUsed(process, artifact, role, account);
	}


	public ProvenanceContextFacade getGraph() { return graph; }


	/**
	 * @return the account
	 */
	public ProvenanceAccount getAccount() { return currentAccount; }


	/**
	 * @param account the account to set
	 */
	public void setAccount(ProvenanceAccount account) { this.currentAccount = account; }

	/**
	 * @return the currentRole
	 */
	public ProvenanceRole getCurrentRole() { return currentRole; }

	/**
	 * @param currentRole the currentRole to set
	 */
	public void setCurrentRole(ProvenanceRole currentRole) { this.currentRole = currentRole; }

	/**
	 * @return the currentArtifact
	 */
	public ProvenanceArtifact getCurrentArtifact() { return currentArtifact; }

	/**
	 * @param currentArtifact the currentArtifact to set
	 */
	public void setCurrentArtifact(ProvenanceArtifact currentArtifact) { this.currentArtifact = currentArtifact; }

	/**
	 * @return the currentAccount
	 */
	public ProvenanceAccount getCurrentAccount() { return currentAccount; }

	/**
	 * @param currentAccount the currentAccount to set
	 */
	public void setCurrentAccount(ProvenanceAccount currentAccount) { this.currentAccount = currentAccount; }

	/**
	 * @return the currentProcess
	 */
	public ProvenanceProcess getCurrentProcess() { return currentProcess; }

	/**
	 * @param currentProcess the currentProcess to set
	 */
	public void setCurrentProcess(ProvenanceProcess currentProcess) { this.currentProcess = currentProcess; }

	public String writeGraph() {

		// print out OPM graph in RDF/XML form
		try {
			Set<Triple> allTriples = context.getTriples();

			RdfXmlWriter writer = new RdfXmlWriter();	
			StringWriter sw = new StringWriter();
			writer.write(allTriples, sw);
			return sw.toString();

		} catch (OperatorException e) {
			logger.error("Could not write graph", e);
		} catch (IOException e) {
			logger.error("Could not write graph", e);
		}	
		return null;
	}



	/**
	 * IN THE RELEASE WE DO NOT SUPPORT XML -- ONE CAN CONVERT THE RDF TO XML OUT-OF-BAND
	 * simply invokes the org.openprovenance for converting an RDF OPM graph to an XML OPM graph
	 * @return a hard-coded filename for the converted XML OPM graph
	 * @throws OperatorException
	 * @throws IOException
	 * @throws JAXBException
	 */
//	public String Rdf2Xml() throws OperatorException, IOException, JAXBException {
//
//		OPMRdf2Xml converter = new OPMRdf2Xml();
//		converter.convert(OPM_RDF_GRAPH_FILE, OPM_XML_GRAPH_FILE);		
//		return OPM_XML_GRAPH_FILE;
//	}


	/**
	 * creates a dot file from the current OPMGraph. <br/>
	 * DOT NOT USE NEEDS FIXING
	 * @return
	 * @throws IOException 
	 * @throws OperatorException 
	 */
	public String Rdf2Dot() throws OperatorException, IOException {

		OPMRdf2Xml converter = new OPMRdf2Xml();
		OPMGraph graph = converter.convert(OPM_RDF_GRAPH_FILE);

		List<Process> processes = graph.getProcesses().getProcess();		
		for (Process p:processes) { p.setId("\""+p.getId()+"\""); }

		List<Artifact> artifacts = graph.getArtifacts().getArtifact();		
		for (Artifact a:artifacts) { a.setId("\""+a.getId()+"\""); }

//		OPMToDot aOPMToDot = new OPMToDot(DOT_CONFIG_FILE);  		
		OPMToDot aOPMToDot = new OPMToDot();  		

		aOPMToDot.convert(graph, new File(OPM_DOT_FILE));
		return OPM_DOT_FILE;

	}


	/**
	 * @param graph the graph to set
	 */
	public void setGraph(ProvenanceContextFacade graph) { this.graph = graph; }

	public void setActive(boolean active) { isActive = active; }

	public boolean isActive()  { return isActive; }


}
