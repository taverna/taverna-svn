package ${packageName};

import java.util.HashMap;
import java.util.Map;


import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class MyLocalWorkerTest 
{
	private LocalWorker worker;
	
	@Before
	public void createWorker() {
		worker=new MyLocalWorker();
	}
	
	@Test
	public void execute() throws TaskExecutionException {
		Map<String,DataThing> inputs = new HashMap<String, DataThing>();
		Map<String,DataThing> output = worker.execute(inputs);
		fail("I need to write a test!");
	}
}
