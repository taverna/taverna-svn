package net.sf.taverna.service.queue;

import java.util.Map;

import net.sf.taverna.service.queue.Job.State;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

public class TavernaQueueListener extends QueueListener {

	WorkflowLauncher launcher;
	
	public TavernaQueueListener(TavernaQueue queue) {
		super(queue);
	}

	@SuppressWarnings("unchecked")
	@Override
	void execute(final Job job) throws ScuflException, InvalidInputException {
		launcher = new WorkflowLauncher(job.workflow);
		Map<String, DataThing> results =
			launcher.execute(job.inputs, new JobEventListener(job));
		job.setResults(results);
		updateProgressReport(job);
	}

	// FIXME: Not exactly the fastest web service in town to regenerate that
	// progress XML every time something happens - just because someone might
	// ask for it
	private void updateProgressReport(Job job) {
		if (launcher == null) {
			return;
		}
		String progressReport = launcher.getProgressReportXML();
		job.setProgressReport(progressReport);
	}
	
	public class JobEventListener extends WorkflowEventAdapter {

		private Job job;

		public JobEventListener(Job job) {
			this.job = job;
		}

		@Override
		public void collectionConstructed(CollectionConstructionEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void dataChanged(UserChangedDataEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void nestedWorkflowCompleted(NestedWorkflowCompletionEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void nestedWorkflowCreated(NestedWorkflowCreationEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void nestedWorkflowFailed(NestedWorkflowFailureEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void processCompleted(ProcessCompletionEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void processCompletedWithIteration(IterationCompletionEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void processFailed(ProcessFailureEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void workflowCompleted(WorkflowCompletionEvent arg0) {
			updateProgressReport(job);
			job.setState(State.COMPLETE);
		}

		@Override
		public void workflowCreated(WorkflowCreationEvent arg0) {
			updateProgressReport(job);
		}

		@Override
		public void workflowFailed(WorkflowFailureEvent arg0) {
			updateProgressReport(job);
			job.setState(State.FAILED);
		}

		@Override
		public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent arg0) {
			job.setState(State.DESTROYED);
			launcher = null;
		}

	}

}
