package org.embl.ebi.escience.scuflworkers.wsdl;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

import junit.framework.*;

public class WSDLBasedProcessorTest extends TestCase 
{
	public void testCreation() throws Exception
	{		
		
		Processor processor = new WSDLBasedProcessor(null,"test","http://www.ebi.ac.uk/ws/services/urn:Dbfetch?wsdl","getSupportedDBs");
		
		assertEquals("invalid name","test",processor.getName());
		assertEquals("incorrect number of outputs",2,processor.getOutputPorts().length);
		assertEquals("incorrect output port name","attachmentList",processor.getOutputPorts()[0].getName());
		assertEquals("incorrect output port name","getSupportedDBsReturn",processor.getOutputPorts()[1].getName());
		assertEquals("incorrect output port syntactic type","l('text/plain')",processor.getOutputPorts()[1].getSyntacticType());
				
		
	}
	
	
	public void testCreationFailureBadAddress()
	{
		try
		{
			Processor processor = new WSDLBasedProcessor(null,"fail","http://invalidaddress","fail");
			fail("an exception should have been thrown");
		}
		catch(Exception e)
		{
			assertTrue("incorrect exception thrown, should have been ProcessorCreationException",e instanceof ProcessorCreationException);			
		}
	}
	
	public void testCreationFailureBadService()
	{
		try
		{
			Processor processor = new WSDLBasedProcessor(null,"fail","http://www.ebi.ac.uk/ws/services/urn:Dbfetch?wsdl","invalidservice");
			fail("an exception should have been thrown");
		}
		catch(Exception e)
		{
			assertTrue("incorrect exception thrown, should have been ProcessorCreationException",e instanceof ProcessorCreationException);			
		}
	}
	
	public void testDocumentPortType() throws Exception
	{
		Processor processor = new WSDLBasedProcessor(null,"test","http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl","run_eGquery");
		
		assertEquals("invalid name","test",processor.getName());
		assertEquals("incorrect number of outputs",2,processor.getOutputPorts().length);
		assertEquals("incorrect output port name","attachmentList",processor.getOutputPorts()[0].getName());
		assertEquals("incorrect output port name","parameters",processor.getOutputPorts()[1].getName());
		assertEquals("incorrect output port syntactic type","'text/xml'",processor.getOutputPorts()[1].getSyntacticType());
		
	}
	
}
