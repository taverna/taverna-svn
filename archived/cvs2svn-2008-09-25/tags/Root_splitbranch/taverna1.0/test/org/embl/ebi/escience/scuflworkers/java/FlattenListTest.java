package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;

public class FlattenListTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[]{"inputlist"};
	}

	protected String[] expectedOutputNames() {
		return new String[]{"outputlist"};
	}

	protected String[] expectedInputTypes() {
		return new String[]{"l(l(''))"};
	}

	protected String[] expectedOutputTypes() {
		return new String[]{"l('')"};
	}

	protected LocalWorker getLocalWorker() {
		return new FlattenList();
	}
	
	public void testFlattening() throws Exception
	{
		List list=new ArrayList();
		List list1=new ArrayList();
		List list2=new ArrayList();
		List list3=new ArrayList();
		List list4=new ArrayList();
		
		list1.add("1");
		list1.add("2");
		list1.add("3");
		
		list2.add("4");
		list2.add("5");
		list2.add("6");
		list2.add("7");
		
		list3.add("8");
		list3.add("9");
		
		list4.add("10");
		
		list.add(list1);
		list.add(list2);
		list.add(list3);
		list.add(list4);
		
		Map inputs=new HashMap();
		inputs.put("inputlist",new DataThing(list));
		
		Map out = new FlattenList().execute(inputs);
		
		DataThing thing=(DataThing)out.get("outputlist");
		List result = (List)thing.getDataObject();
		
		assertEquals("incorrect number of entries in the list",10,result.size());
		for (int i=1;i<=10;i++)
		{
			String testVal=String.valueOf(i);
			assertEquals("incorrect value in list",testVal,result.get(i-1));			
		}
		
		
		
	}

}
