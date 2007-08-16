package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

public class StringSetIntersectionTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[]{"list1","list2"};
	}

	protected String[] expectedOutputNames() {
		return new String[]{"intersection"};
	}

	protected String[] expectedInputTypes() {
		return new String[]{"l('text/plain')","l('text/plain')"};
	}

	protected String[] expectedOutputTypes() {
		return new String[]{"l('text/plain')"};
	}

	protected LocalWorker getLocalWorker() {
		return new StringSetIntersection();
	}
	
	public void testIntersection() throws Exception
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
		
		Map output=new StringSetIntersection().execute(inputs);
		DataThing thing = (DataThing)output.get("intersection");
		
		assertNotNull("no result found",thing);
		
		List result = (List)thing.getDataObject();
		
		assertEquals("incorrect number of elements",2,result.size());
		assertTrue("results should contain string1",result.contains("string1"));
		assertTrue("results should contain string5",result.contains("string5"));
	}

}
