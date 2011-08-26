package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

public class StringSetUnionTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[] { "list1", "list2" };
	}

	protected String[] expectedInputTypes() {
		return new String[] { "l('text/plain')", "l('text/plain')" };
	}

	protected String[] expectedOutputNames() {
		return new String[] { "union" };
	}

	protected String[] expectedOutputTypes() {
		return new String[] { "l('text/plain')" };
	}

	protected LocalWorker getLocalWorker() {
		return new StringSetUnion();
	}
	
	public void testUnion() throws Exception
	{
		List list1=new ArrayList();
		List list2=new ArrayList();
		
		list1.add("string1");
		list1.add("string3");
		list1.add("string4");
		list1.add("string5");
		
		list2.add("string1");
		list2.add("string2");
		list2.add("string5");
		list2.add("string6");
		
		Map inputs = new HashMap();
		inputs.put("list1",new DataThing(list1));
		inputs.put("list2",new DataThing(list2));
		
		Map output=new StringSetUnion().execute(inputs);
		DataThing thing = (DataThing)output.get("union");
		
		assertNotNull("no result found",thing);
		
		List result = (List)thing.getDataObject();
		
		assertEquals("incorrect number of elements",6,result.size());
		
		String [] expected = {"string1","string2","string3","string4","string5","string6"};
		
		for (int i=0;i<6;i++)
		{
			assertTrue("list should contain the string "+expected[i],result.contains(expected[i]));
		}
	}

}
