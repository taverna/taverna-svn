package net.sf.taverna.t2.performance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.junit.Test;

public class TestPerformance {
	
	@Test
	public void compareTavernas() throws WorkflowSubmissionException, IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		
		PerformanceTester tester = new PerformanceTester();
		InputStream inStream = TestPerformance.class
		.getResourceAsStream("/iterator_workflow.xml");
		
		tester.init(inStream, tmpDir.getAbsolutePath());
		Map<String, DataThing> t1InputMap = new HashMap<String, DataThing>();
		Long runT1 = tester.runT1(10, t1InputMap);
		Map<DataflowInputPort, EntityIdentifier> t2InputMap = new HashMap<DataflowInputPort, EntityIdentifier>();
		Long runT2 = tester.runT2(10, t2InputMap);
		System.out.println("T1 took " + runT1 + "\nT2 took " + runT2);
	}

}
