package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;

import java.util.*;

public class StringSetDifferenceTest extends LocalWorkerTestCase 
{

	protected String[] expectedInputNames() {
		return new String[] { "list1", "list2" };
	}

	protected String[] expectedInputTypes() {
		return new String[] { "l('text/plain')", "l('text/plain')" };
	}

	protected String[] expectedOutputNames() {
		return new String[] { "difference" };
	}

	protected String[] expectedOutputTypes() {
		return new String[] { "l('text/plain')" };
	}

	protected LocalWorker getLocalWorker() {
		return new StringSetDifference();
	}
	
	public void testDifference() throws Exception
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
		
		Map output=new StringSetDifference().execute(inputs);
		DataThing thing = (DataThing)output.get("difference");
		
		assertNotNull("no result found",thing);
		
		List result = (List)thing.getDataObject();
		
		assertEquals("incorrect number of elements",4,result.size());
		
		String [] expected = {"string2","string3","string4","string6"};
		
		for (int i=0;i<expected.length;i++)
		{			
			assertTrue("expected string ("+expected[i]+") not found in list",result.contains(expected[i]));
		}
		
	}

}
