package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;

public class PadNumberTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[]{"input","targetlength"};
	}

	protected String[] expectedOutputNames() {
		return new String[]{"padded"};
	}

	protected String[] expectedInputTypes() {
		return new String[]{"'text/plain'","'text/plain'"};
	}

	protected String[] expectedOutputTypes() {
		return new String[]{"'text/plain'"};
	}

	protected LocalWorker getLocalWorker() {
		return new PadNumber();
	}
	
	public void testDefault() throws Exception
	{
		Map inputs = new HashMap();
		inputs.put("input",new DataThing("123"));
		
		Map output = new PadNumber().execute(inputs);
		
		DataThing thing = (DataThing)output.get("padded");
		assertNotNull("no result found",thing);
		
		String result = (String)thing.getDataObject();
		
		assertEquals("incorrect result","0000123",result);
	}
	
	public void testPadding() throws Exception
	{
		Map inputs = new HashMap();
		inputs.put("input",new DataThing("12345"));
		inputs.put("targetlength",new DataThing("15"));
		
		Map output = new PadNumber().execute(inputs);
		
		DataThing thing = (DataThing)output.get("padded");
		assertNotNull("no result found",thing);
		
		String result = (String)thing.getDataObject();
		
		assertEquals("incorrect result","000000000012345",result);
	}
	
	public void testPadding2() throws Exception
	{
		Map inputs = new HashMap();
		inputs.put("input",new DataThing("123456789"));
		inputs.put("targetlength",new DataThing("12"));
		
		Map output = new PadNumber().execute(inputs);
		
		DataThing thing = (DataThing)output.get("padded");
		assertNotNull("no result found",thing);
		
		String result = (String)thing.getDataObject();
		
		assertEquals("incorrect result","000123456789",result);
	}

}
