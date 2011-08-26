package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

public class ByteArrayToStringTest extends LocalWorkerTestCase 
{
	public void testByteToArray() throws Exception
	{
		byte [] bytes = { '1', '2', '3', '4', '5'};
		Map inputs = new HashMap();
		inputs.put("bytes",new DataThing(bytes));
		
		Map outputs = new ByteArrayToString().execute(inputs);
		
		assertEquals("outputs should contain 1 element",1,outputs.size());
		Object result=outputs.get("string");
		assertNotNull("element in map for 'string' should not be null",result);
		assertTrue("element in map should be of type DataThing",result instanceof DataThing);
		assertEquals("value of string is incorrect","12345",((DataThing)result).getDataObject());
		
	}
		
	
	protected String[] expectedInputNames() {
		return new String [] {"bytes"};
	}

	protected String[] expectedInputTypes() {
		return new String [] {"'application/octet-stream'"};
	}

	protected String[] expectedOutputNames() {
		return new String []{"string"};
	}

	protected String[] expectedOutputTypes() {
		return new String []{"'text/plain'"};
	}

	protected LocalWorker getLocalWorker() {
		return new ByteArrayToString();
	}
}
