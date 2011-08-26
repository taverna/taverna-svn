package org.embl.ebi.escience.scuflworkers.java;

import java.util.*;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;

public class StringConcatTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[]{"string1","string2"};
	}

	protected String[] expectedOutputNames() {
		return new String[]{"output"};
	}

	protected String[] expectedInputTypes() {
		return new String[]{"'text/plain'","'text/plain'"};
	}

	protected String[] expectedOutputTypes() {
		return new String[]{"'text/plain'"};
	}

	protected LocalWorker getLocalWorker() {
		return new StringConcat();
	}
	
	public void testConcat() throws Exception
	{
		Map inputs=new HashMap();
		inputs.put("string1",new DataThing("bob "));
		inputs.put("string2",new DataThing("monkhouse"));
		
		Map out = new StringConcat().execute(inputs);
		
		assertEquals("there should only be 1 output",1,out.size());
		
		DataThing thing = (DataThing)out.get("output");
		String result=(String)thing.getDataObject();
		assertEquals("output is wrong","bob monkhouse",result);		
	}

}
