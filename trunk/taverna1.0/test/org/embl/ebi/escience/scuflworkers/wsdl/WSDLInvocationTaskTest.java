package org.embl.ebi.escience.scuflworkers.wsdl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;

import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.task.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

public class WSDLInvocationTaskTest extends TestCase 
{
	public void testExecute() throws Exception
	{
		Processor processor = new WSDLBasedProcessor(null,"guid","http://webservices.oracle.com/ws/guid/oracle.ws.OTNGUIDGenerator?WSDL","getGUID");
		ProcessorTask procTask=new ProcessorTask("id",new Flow(null,null),processor, new LogLevel(LogLevel.NONE),"bob","ctx");
		WSDLInvocationTask task = new WSDLInvocationTask(processor);
		Map result=task.execute(new HashMap(),procTask);
		assertEquals("incorrect number of results",2,result.size());
		DataThing thing = (DataThing)result.get("return");
		assertTrue("guid returned should be a string",thing.getDataObject() instanceof String);
		assertNotNull("no attachmentlist",result.get("attachmentList"));		
	}
		
}
