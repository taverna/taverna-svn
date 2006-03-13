package org.embl.ebi.escience.scuflworkers.wsdl;

import java.util.*;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;

import junit.framework.TestCase;

public class WSDLInvocationTaskTest extends TestCase 
{
	public void testExecute() throws Exception
	{
		Processor processor = new WSDLBasedProcessor(null,"guid","http://webservices.oracle.com/ws/guid/oracle.ws.OTNGUIDGenerator?WSDL","getGUID");
		WSDLInvocationTask task = new WSDLInvocationTask(processor);
		Map result=task.execute(new HashMap(),null);
		assertEquals("incorrect number of results",2,result.size());
		DataThing thing = (DataThing)result.get("return");
		assertTrue("guid returned should be a string",thing.getDataObject() instanceof String);
		assertNotNull("no attachmentlist",result.get("attachmentList"));		
	}
	
	/* Commented out for now, because the service isn't working half the time.
	public void testExecute2() throws Exception
	{
		Processor processor = new WSDLBasedProcessor(null,"serviceAlive","http://soap.bind.ca/wsdl/bind.wsdl","isServiceAlive");
		WSDLInvocationTask task = new WSDLInvocationTask(processor);
		Map result=task.execute(new HashMap(),null);
		assertEquals("incorrect number of results",2,result.size());
		DataThing thing = (DataThing)result.get("isServiceAliveReturn");
		String isAlive = (String)thing.getDataObject();
		assertTrue("incorrect result, should be true or false.",isAlive.equalsIgnoreCase("true")||isAlive.equalsIgnoreCase("false"));		
		assertNotNull("no attachmentlist",result.get("attachmentList"));		
	}
	*/
}
