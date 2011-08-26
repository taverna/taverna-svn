package org.embl.ebi.escience.scuflworkers.soaplab;

import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.ProcessorCreationException;

import junit.framework.TestCase;

public class SoaplabProcessorTest extends TestCase 
{
	public void testCreation() throws Exception
	{
		SoaplabProcessor proc = new SoaplabProcessor(null,"helloworld","http://www.ebi.ac.uk/soaplab/services/gowlab.helloworld");
		OutputPort [] ports = proc.getOutputPorts();
		assertEquals("incorrect number of outputs",2,ports.length);
		assertEquals("incorrect output name","report",ports[0].getName());
		assertEquals("incorrect output name","result",ports[1].getName());
	}
	
	public void testCreationFailure()
	{
		try
		{
			SoaplabProcessor proc = new SoaplabProcessor(null,"invalid","http://invalidaddress");
			fail("an exception should have been thrown");
		}
		catch(Exception e)
		{
			assertTrue("exception is of the wrong type",e instanceof ProcessorCreationException);
		}
	}
}
