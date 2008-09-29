package ${packageName};

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;


/**
 * TODO: document my worker
 * 
 * @author 
 *
 */
public class MyLocalWorker implements LocalWorker 
{
	private static Logger logger = Logger.getLogger(MyLocalWorker.class);

	//TODO: define your input names
	public String [] inputNames() {
		return new String [] {};
	}

	//TODO: define your input mime types
	public String [] inputTypes() {
		return new String[] {};
	}

	//TODO: define your output names
	public String [] outputNames() {
		return new String[] {};
	}

	//TODO: define your output mime types
	public String [] outputTypes() {
		return new String[] {};
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		Map<String,DataThing> result = new HashMap<String,DataThing>();
		try {
			//TODO: your execution code goes here	
		}
		catch(Exception e) {
			logger.error("Error in execute!",e);
			throw new TaskExecutionException(e);
		}
		return result;
	}
}
