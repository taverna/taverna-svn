package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

public class StringListMergeTest extends LocalWorkerTestCase 
{

	protected String[] expectedInputNames() {
		return new String[]{"stringlist","seperator"};
	}

	protected String[] expectedInputTypes() {
		return new String[]{"l('text/plain')","'text/plain'"};
	}

	protected String[] expectedOutputNames() {
		return new String[]{"concatenated"};
	}

	protected String[] expectedOutputTypes() {
		return new String[]{"'text/plain'"};
	}

	protected LocalWorker getLocalWorker() {
		return new StringListMerge();
	}
	
	public void testMerge() throws Exception
	{
		List list=new ArrayList();
		list.add("string1");
		list.add("string2");
		list.add("string3");
		list.add("string4");
		list.add("string5");
							
		Map inputs=new HashMap();
		inputs.put("stringlist",new DataThing(list));
		inputs.put("seperator",new DataThing(","));
		
		Map outputs=new StringListMerge().execute(inputs);
		DataThing out = (DataThing)outputs.get("concatenated");
		assertNotNull("no output producted",out);
		
		String result = (String) out.getDataObject();
		
		assertEquals("concatenated incorrectly","string1,string2,string3,string4,string5",result);		
	}
	
}
