package net.sf.taverna.service.queue;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.util.XMLUtils;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
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
import org.embl.ebi.escience.scufl.enactor.event.WorkflowInstanceEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

public class TavernaQueueListener extends QueueListener {

	// 60 seconds is "long running" while developing
	private static final long JOB_TIMEOUT = 60 *  1000;

	private static Logger logger = Logger.getLogger(TavernaQueueListener.class);

	private static DAOFactory daoFactory = DAOFactory.getFactory();
	
	private JobDAO jobDao = daoFactory.getJobDAO();
	
	private DataDocDAO dataDocDao = daoFactory.getDataDocDAO();
	
	Map<WorkflowInstance, Job> jobs = new HashMap<WorkflowInstance, Job>();

	Map<Job, WorkflowInstance> wfInstances =
		new HashMap<Job, WorkflowInstance>();
	
	Map<WorkflowInstance, Object> locks =
		new HashMap<WorkflowInstance, Object>();

	public TavernaQueueListener(TavernaQueue queue) {
		super(queue);
		WorkflowEventDispatcher.DISPATCHER.addListener(new JobEventListener());
	}

	@SuppressWarnings("unchecked")
	@Override
	void execute(final Job job) throws ScuflException, InvalidInputException, ParseException {
		EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
		
		ScuflModel workflow = XMLUtils.parseXScufl(job.getWorkflow().getScufl());
		Map<String, DataThing> inputs = job.getInputs().getDataMap();
		
		WorkflowInstance workflowInstance =
			enactor.compileWorkflow(workflow, inputs, null);
		jobs.put(workflowInstance, job);
		wfInstances.put(job, workflowInstance);
		Object lock = new Object();
		synchronized (locks) {
			locks.put(workflowInstance, lock);
			workflowInstance.run();
			synchronized (lock) {
				try {
					// The rest of the work will be done by the
					// JobEventListener, we'll just wait here
					// until he releases our lock (until we hit the timeout, 
					// then we'll go on with the next job)
					lock.wait(JOB_TIMEOUT);
				} catch (InterruptedException e) {
					logger.debug("Interrupted from lock: " + job);
				}
			}
		}
	}

	public class JobEventListener extends WorkflowEventAdapter {
		// FIXME: Not exactly the fastest web service in town to regenerate that
		// progress XML every time something happens - just because someone
		// might ask for it
		private void updateProgressReport(WorkflowInstanceEvent event) {
			Job job = jobFromEvent(event);
			if (job != null) {
				String progressReport =
					event.getWorkflowInstance().getProgressReportXMLString();
				job.setProgressReport(progressReport);
			}
		}

		private Job jobFromEvent(WorkflowInstanceEvent event) {
			WorkflowInstance workflowInstance = event.getWorkflowInstance();
			return jobs.get(workflowInstance);
		}

		@Override
		public void collectionConstructed(CollectionConstructionEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void dataChanged(UserChangedDataEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void nestedWorkflowCompleted(NestedWorkflowCompletionEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void nestedWorkflowCreated(NestedWorkflowCreationEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void nestedWorkflowFailed(NestedWorkflowFailureEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void processCompleted(ProcessCompletionEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void processCompletedWithIteration(IterationCompletionEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void processFailed(ProcessFailureEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void workflowCompleted(WorkflowCompletionEvent event) {
			try {
				updateProgressReport(event);
				Job job = jobFromEvent(event);
				Map<String, DataThing> outputs = event.getWorkflowInstance().getOutput();
				DataDoc outputDoc = new DataDoc();
				outputDoc.setDataMap(outputs);
				job.setOutputDoc(outputDoc);
				job.setStatus(Status.COMPLETE);
				dataDocDao.create(outputDoc);
				jobDao.update(job);
				daoFactory.commit();
			} finally {
				Object lock = locks.get(event.getWorkflowInstance());
				synchronized (lock) {
					lock.notifyAll();
				}
			}
			event.getWorkflowInstance().destroy();
		}

		@Override
		public void workflowCreated(WorkflowCreationEvent event) {
			updateProgressReport(event);
		}

		@Override
		public void workflowFailed(WorkflowFailureEvent event) {
			try {
				updateProgressReport(event);
				Job job = jobFromEvent(event);
				job.setStatus(Status.FAILED);
				jobDao.update(job);
				daoFactory.commit();
			} finally {
				Object lock = locks.get(event.getWorkflowInstance());
				synchronized (lock) {
					lock.notifyAll();
				}

			}
			event.getWorkflowInstance().destroy();
		}

		@Override
		public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent event) {
			Job job = jobFromEvent(event);
			// Don't set as DESTROYED, since we keep the results outside it is
			// "complete" for the queue's concern
			// job.setState(Status.DESTROYED);
			wfInstances.remove(job);
			WorkflowInstance wfInstance = event.getWorkflowInstance();
			jobs.remove(wfInstance);
			synchronized (locks) {
				locks.remove(wfInstance);
			}
		}

	}

}
