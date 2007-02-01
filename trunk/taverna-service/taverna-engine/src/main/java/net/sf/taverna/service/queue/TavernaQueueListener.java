package net.sf.taverna.service.queue;

import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

public class TavernaQueueListener extends QueueListener {

	public TavernaQueueListener(TavernaQueue queue) {
		super(queue);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void execute(Job job) throws ScuflException, InvalidInputException {
		WorkflowLauncher launcher = new WorkflowLauncher(job.workflow);
		Map<String, DataThing> results = launcher.execute(job.inputs);
		job.setResults(results);
		String progressReport = launcher.getProgressReportXML();
		job.setProgressReport(progressReport);
	}	

}
