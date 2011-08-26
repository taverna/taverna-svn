package org.embl.ebi.escience.scuflworkers.java;

import java.util.*;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import junit.framework.TestCase;

public class FailIfFalseTest extends TestCase 
{
	public void testOnNotTrue() throws Exception
	{
		DataThing thing=new DataThing("false");
		FailIfFalse ifFalse = new FailIfFalse();
		HashMap inputs=new HashMap();
		inputs.put("test",thing);
		try
		{
			ifFalse.execute(inputs);
			fail("TaskExecutionException should have been thrown");
		}
		catch(TaskExecutionException e)
		{
			
		}		
	}
	
	public void testOnTrue() throws Exception
	{
		DataThing thing=new DataThing("true");
		FailIfFalse ifFalse = new FailIfFalse();
		HashMap inputs=new HashMap();
		inputs.put("test",thing);
		try
		{
			Map output=ifFalse.execute(inputs);	
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
		return new FailIfFalse();
	}
}
