package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;
import java.util.*;

public class FilterStringListTest extends LocalWorkerTestCase {

	public void testFiltering() throws Exception
	{
		List stringlist=new ArrayList();
		stringlist.add("monkey");
		stringlist.add("cheese");
		stringlist.add("cheesecake");
		stringlist.add("carrotcake");
		stringlist.add("parrot");
		stringlist.add("chocolatecake");
		FilterStringList filter=new FilterStringList();
		Map inputs=new HashMap();
		inputs.put("stringlist", new DataThing(stringlist));
		inputs.put("regex", new DataThing(".*cake"));
		
		Map out=filter.execute(inputs);
		assertEquals("there should only be 1 output",1,out.size());
		DataThing thing=(DataThing)out.get("filteredlist");
		
		List outlist=(List)thing.getDataObject();
		assertEquals("returned list should contain 3 elements",3,outlist.size());
		assertTrue("list should contain cheesecake",outlist.contains("cheesecake"));
		assertTrue("list should contain carrotcake",outlist.contains("carrotcake"));
		assertTrue("list should contain chocolatecake",outlist.contains("chocolatecake"));
	}
	
	public void testFiltering2() throws Exception
	{
		List stringlist=new ArrayList();
		stringlist.add("monkey");
		stringlist.add("cheese");
		stringlist.add("cheesecake");
		stringlist.add("carrotcake");
		stringlist.add("parrot");
		stringlist.add("chocolatecake");
		FilterStringList filter=new FilterStringList();
		Map inputs=new HashMap();
		inputs.put("stringlist", new DataThing(stringlist));
		inputs.put("regex", new DataThing("cheese.*"));
		
		Map out=filter.execute(inputs);
		assertEquals("there should only be 1 output",1,out.size());
		DataThing thing=(DataThing)out.get("filteredlist");
		
		List outlist=(List)thing.getDataObject();
		assertEquals("returned list should contain 2 elements",2,outlist.size());
		assertTrue("list should contain cheese",outlist.contains("cheese"));
		assertTrue("list should contain cheesecake",outlist.contains("cheesecake"));		
	}
	
	protected String[] expectedInputNames() {
		return new String [] {"stringlist","regex"};
	}

	protected String[] expectedOutputNames() {
		return new String [] {"filteredlist"};
	}

	protected String[] expectedInputTypes() {
		return new String [] {"l('text/plain')","'text/plain'"};
	}

	protected String[] expectedOutputTypes() {
		return new String [] {"l('text/plain')"};
	}

	protected LocalWorker getLocalWorker() {
		return new FilterStringList();
	}

}
