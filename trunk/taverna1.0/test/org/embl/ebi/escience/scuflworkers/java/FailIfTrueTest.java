package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.LocalWorkerTestCase;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class FailIfTrueTest extends LocalWorkerTestCase 
{
	public void testOnTrue() throws Exception
	{
		DataThing thing=new DataThing("true");
		FailIfTrue ifTrue = new FailIfTrue();
		HashMap inputs=new HashMap();
		inputs.put("test",thing);
		try
		{
			ifTrue.execute(inputs);
			fail("TaskExecutionException should have been thrown");
		}
		catch(TaskExecutionException e)
		{
			
		}
		
	}
	
	public void testOnNotTrue() throws Exception
	{
		DataThing thing=new DataThing("false");
		FailIfTrue ifTrue = new FailIfTrue();
		HashMap inputs=new HashMap();
		inputs.put("test",thing);
		try
		{
			Map output=ifTrue.execute(inputs);
			assertEquals("returned map should be empty",0,output.size());
		}
		catch(TaskExecutionException e)
		{
			fail("TaskExecutionException should not have been thrown");
		}		
	}

	protected String[] expectedInputNames() {
		return new String [] {"test"};
	}

	protected String[] expectedInputTypes() {
		return new String [] {"'text/plain'"};
	}

	protected String[] expectedOutputNames() {
		return new String [0];
	}

	protected String[] expectedOutputTypes() {
		return new String [0];
	}

	protected LocalWorker getLocalWorker() {
		return new FailIfTrue();
	}
	
	
	
	
}
