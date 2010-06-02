package net.sf.taverna.t2.lineageService.analysis.test;


import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tupeloproject.cet.model.Artifact;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.UnionContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.kernel.impl.ResourceContext;
import org.tupeloproject.provenance.ProvenanceAccount;
import org.tupeloproject.provenance.ProvenanceArtifact;
import org.tupeloproject.provenance.ProvenanceProcess;
import org.tupeloproject.provenance.ProvenanceRole;
import org.tupeloproject.provenance.impl.ProvenanceContextFacade;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.xml.RdfXmlWriter;

public class OPMTest {

	private static final String OPM_TAVERNA_NAMESPACE = "http://taverna.opm.org/";
	private static final String OPM_GRAPH_FILE = "src/test/resources/provenance-testing/OPM/OPMGraph.rdf";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void OPMTest() throws Exception {
		
	Context context = null;
	ProvenanceContextFacade graph = null;
	ProvenanceAccount account = null;

	// init Tupelo RDF provenance graph
	MemoryContext mc = new MemoryContext();
	ResourceContext rc = new ResourceContext("http://example.org/data/","/provenanceExample/");
	 context = new UnionContext();
	context.addChild(mc);
	context.addChild(rc);

	graph = new ProvenanceContextFacade(context);
	
	account = 
		graph.newAccount("OPMTest", Resource.uriRef(OPM_TAVERNA_NAMESPACE+"account1"));
	
	graph.assertAccount(account);

	Resource ar1 = Resource.uriRef(OPM_TAVERNA_NAMESPACE+"artifact1");
	ProvenanceArtifact a1 = graph.newArtifact("artifact 1", ar1);
	graph.assertArtifact(a1);
	
	Resource ar2 = Resource.uriRef(OPM_TAVERNA_NAMESPACE+"artifact2");
	ProvenanceArtifact a2 = graph.newArtifact("artifact 2", ar2);
	graph.assertArtifact(a2);
	
	ProvenanceProcess p1 = graph.newProcess("process 1");
	graph.assertProcess(p1);
	
	ProvenanceRole role1 = graph.newRole("r1");
	ProvenanceRole role2 = graph.newRole("r2");

	graph.assertUsed(p1, a1, role1, account);
	graph.assertGeneratedBy(a2, p1, role2, account);
	
	// print out OPM graph for diagnostics
	try {
		Set<Triple> allTriples = context.getTriples();
		
		RdfXmlWriter writer = new RdfXmlWriter();				
		writer.write(allTriples, new FileWriter(OPM_GRAPH_FILE));
		
		System.out.println("OPM graph written to "+OPM_GRAPH_FILE);
		
	} catch (OperatorException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	}

}
