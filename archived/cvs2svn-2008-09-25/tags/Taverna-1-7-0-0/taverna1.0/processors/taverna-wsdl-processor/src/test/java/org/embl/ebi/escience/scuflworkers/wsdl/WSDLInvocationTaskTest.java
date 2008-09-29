package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.task.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class WSDLInvocationTaskTest extends WSDLBasedTestCase {
	
	public void setUp() throws IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		assertTrue(tmpDir.delete());
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		assertTrue(tmpDir.isDirectory());
		TavernaSPIRegistry.setRepository(tempRepository);		
	}
	

	public void testExecute() throws ScuflException, TaskExecutionException  {
		Processor processor = new WSDLBasedProcessor(
				null,
				"guid",
				TESTWSDL_BASE+"GUIDGenerator.wsdl",
				"getGUID");
		ProcessorTask procTask = new ProcessorTask("id", new Flow(null, null),
				processor, new LogLevel(LogLevel.NONE), "bob", "ctx");
		WSDLInvocationTask task = new WSDLInvocationTask(processor);
		Map result = task.execute(new HashMap(), procTask);
		assertEquals("incorrect number of results", 2, result.size(	));
		DataThing thing = (DataThing) result.get("return");
		assertTrue("guid returned should be a string",
				thing.getDataObject() instanceof String);
		assertNotNull("no attachmentlist", result.get("attachmentList"));
	}

}
