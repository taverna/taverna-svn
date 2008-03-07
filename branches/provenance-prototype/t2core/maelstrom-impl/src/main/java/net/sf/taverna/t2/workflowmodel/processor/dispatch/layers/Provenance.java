package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.NotifiableLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

import org.apache.log4j.Logger;

public class Provenance extends AbstractDispatchLayer<ProvenanceConfig> implements NotifiableLayer {
	
	private static Logger logger = Logger.getLogger(Provenance.class);
	
	private Map<String, StateModel> stateMap = new HashMap<String, StateModel>();
	
	private ProvenanceConfig config = new ProvenanceConfig();
	
	public Provenance() {
		super();
	}
	
	public Provenance(int maxJobs) {
		super();
		config.setMaxJobs(maxJobs);
	}

	public ProvenanceConfig getConfiguration() {
		return config;
	}

	public void configure(ProvenanceConfig config) {
		this.config = config;
	}

	@Override
	public void receiveError(DispatchErrorEvent errorEvent) {
		logger.info("Provenance layer received error event");
//		System.out.println("Provenance layer received error event");
		InvocationContext context = errorEvent.getContext();
		ProvenanceConnector provenanceManager = context.getProvenanceManager();
		Set<? extends AnnotationChain> annotations = errorEvent
				.getFailedActivity().getAnnotations();
		for (AnnotationChain annotation : annotations) {
			List<AnnotationAssertion<?>> assertions = annotation
					.getAssertions();
			for (AnnotationAssertion<?> assertion : assertions) {
				assertion.getCreationDate();
				assertion.getCreators();
				assertion.getCurationAssertions();
				assertion.getDetail();
				// etc. do something with these using the Provenance Manager
				// from the context
			}
		}

		// push up
		StateModel model = stateMap.get(errorEvent.getOwningProcess());
		getAbove().receiveError(errorEvent);
		model.finishWith(errorEvent.getIndex());
	}

	@Override
	public void receiveJob(DispatchJobEvent jobEvent) {
		logger.info("Provenance layer received job event");
//		System.out.println("Provenance layer received job event");
		InvocationContext context = jobEvent.getContext();
		// get something from job event and write to provenance manager?
		// push down
		getBelow().receiveJob(jobEvent);
	}

	@Override
	public void receiveJobQueue(DispatchJobQueueEvent jobQueueEvent) {
		logger.info("Provenance layer received job queue event");
//		System.out.println("Provenance layer received job queue event");
//		System.out.println(jobQueueEvent.getOwningProcess());
		InvocationContext context = jobQueueEvent.getContext();
		ProvenanceConnector provenanceManager = context.getProvenanceManager();
		List<? extends Activity<?>> activities = jobQueueEvent.getActivities();
		for (Activity<?> activity : activities) {
			System.out.println("activity type: "
					+ activity.getClass().getName());
			for (ActivityInputPort activityInputPort : activity.getInputPorts()) {
				// do something with them
			}
			for (OutputPort outputPort : activity.getOutputPorts()) {
				// do something with them
			}

			Set<? extends AnnotationChain> annotations = activity
					.getAnnotations();
			for (AnnotationChain annotation : annotations) {
				for (AnnotationAssertion<?> assertion : annotation
						.getAssertions()) {
//					System.out.println(assertion.getCreationDate());
//					System.out.println(assertion.getCreators());
//					System.out.println(assertion.getCurationAssertions());
//					System.out.println(assertion.getDetail());
					// etc. do something with these using the Provenance Manager
					// from the context
				}
			}
			// do something with them
		}
		// push down
		//don't really get this but it's what the Parallelize layer does
		StateModel model = new StateModel(jobQueueEvent, config.getMaxJobs());
		stateMap.put(jobQueueEvent.getOwningProcess(), model);
		model.fillFromQueue();
		//does it need to push stuff down?
//		getBelow().receiveJobQueue(jobQueueEvent);
	}

	/**
	 * Receive results from layer below in the dispatch stack. Create an XML
	 * representation of the results and send to the {@link ProvenanceConnector}
	 * in the {@link DispatchStack}
	 */
	@Override
	public void receiveResult(DispatchResultEvent resultEvent) {
		logger.info("Provenance layer received result event");
//		System.out.println("Provenance layer received result event");
		InvocationContext context = resultEvent.getContext();
		ProvenanceConnector provenanceConnector = context
				.getProvenanceManager();
		// do something with owning process and
		Map<String, EntityIdentifier> data = null;
		try {
			data = resultEvent.getData();
		} catch (Exception e) {
			logger.warn("there is no data in result");
		}
		String owningProcess = null;
		try {
			owningProcess = resultEvent.getOwningProcess();
		} catch (Exception e) {
			logger.warn("there is no owning process in result");
		}
		if (data != null) {
			String results = "<results>" + "<owner>" + owningProcess
					+ "</owner>\n";
			results = results + "<streaming>" + resultEvent.isStreamingEvent()
					+ "</streaming>\n";

			for (Entry<String, EntityIdentifier> entry : data.entrySet()) {
				// do something with the entry
				results = results + "<result>" + entry.getValue()
						+ "</result>\n";
			}
			// push up
			results = results + "</results>";
			provenanceConnector.saveProvenance(results);
		}
		StateModel model = stateMap.get(resultEvent.getOwningProcess());
		DispatchLayer above = getAbove();
		above.receiveResult(resultEvent);
		model.finishWith(resultEvent.getIndex());
	}

	@Override
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		logger.info("Provenance layer received completion event");
//		System.out.println("Provenance layer received completion event");
		InvocationContext context = completionEvent.getContext();
		StateModel model = stateMap.get(completionEvent.getOwningProcess());
		getAbove().receiveResultCompletion(completionEvent);
		model.finishWith(completionEvent.getIndex());
	}

	@Override
	public void finishedWith(String owningProcess) {
		// no idea what this is supposed to do but this is what Parallelize does
		stateMap.remove(owningProcess);
	}

	public void eventAdded(String owningProcess) {
		if (! stateMap.containsKey(owningProcess)) {
			throw new WorkflowStructureException(
					"Should never see this here, it means we've had duplicate completion events from upstream");
		} else {
			synchronized (stateMap.get(owningProcess)) {
				stateMap.get(owningProcess).fillFromQueue();
			}
		}
	}
	/**
	 * Holds the state for a given owning process
	 * 
	 * @author Tom
	 * 
	 */
	class StateModel {

		private DispatchJobQueueEvent queueEvent;

		@SuppressWarnings("unchecked")//suppressed to avoid jdk1.5 error messages caused by the declaration IterationInternalEvent<? extends IterationInternalEvent<?>> e
		private BlockingQueue<IterationInternalEvent> pendingEvents = new LinkedBlockingQueue<IterationInternalEvent>();

		private int activeJobs = 0;

		private int maximumJobs;

		/**
		 * Construct state model for a particular owning process
		 * 
		 * @param owningProcess
		 *            Process to track parallel execution
		 * @param queue
		 *            reference to the queue into which jobs are inserted by the
		 *            iteration strategy
		 * @param activities
		 *            activities to pass along with job events down into the
		 *            stack below
		 * @param maxJobs
		 *            maximum number of concurrent jobs to keep 'hot' at any
		 *            given point
		 */
		protected StateModel(DispatchJobQueueEvent queueEvent, int maxJobs) {
			this.queueEvent = queueEvent;
			this.maximumJobs = maxJobs;
		}

		Integer queueSize() {
			return queueEvent.getQueue().size();
		}

		/**
		 * Poll the queue repeatedly until either the queue is empty or we have
		 * enough jobs pulled from it. The semantics for this are:
		 * <ul>
		 * <li>If the head of the queue is a Job and activeJobs < maximumJobs
		 * then increment activeJobs, add the Job to the pending events list at
		 * the end and send the message down the stack
		 * <li>If the head of the queue is a Completion and the pending jobs
		 * list is empty then send it to the layer above
		 * <li>If the head of the queue is a Completion and the pending jobs
		 * list is not empty then add the Completion to the end of the pending
		 * jobs list and return
		 * </ul>
		 */
		@SuppressWarnings("unchecked") //suppressed to avoid jdk1.5 error messages caused by the declaration IterationInternalEvent<? extends IterationInternalEvent<?>> e
		protected void fillFromQueue() {
			synchronized (this) {
				while (queueEvent.getQueue().peek() != null
						&& activeJobs < maximumJobs) {
					final IterationInternalEvent e = queueEvent
							.getQueue().remove();

					if (e instanceof Completion && pendingEvents.peek() == null) {
						new Thread(new Runnable() {
							public void run() {
								getAbove().receiveResultCompletion(
										new DispatchCompletionEvent(e
												.getOwningProcess(), e
												.getIndex(), e.getContext()));
							}
						}).start();
						// getAbove().receiveResultCompletion((Completion) e);
					} else {
						pendingEvents.add(e);
					}
					if (e instanceof Job) {
						synchronized (this) {
							activeJobs++;
						}
						// sentJobsCount++;
						getBelow()
								.receiveJob(
										new DispatchJobEvent(e
												.getOwningProcess(), e
												.getIndex(), e.getContext(),
												((Job) e).getData(), queueEvent
														.getActivities()));
					}
				}
			}
		}

		/**
		 * Returns true if the index matched an existing Job exactly, if this
		 * method returns false then you have a partial completion event which
		 * should be sent up the stack without modification.
		 * 
		 * @param index
		 * @return
		 */
		@SuppressWarnings("unchecked") //suppressed to avoid jdk1.5 error messages caused by the declaration IterationInternalEvent<? extends IterationInternalEvent<?>> e
		protected boolean finishWith(int[] index) {
			synchronized (this) {

				for (IterationInternalEvent e : new ArrayList<IterationInternalEvent>(
						pendingEvents)) {
					if (e instanceof Job) {
						Job j = (Job) e;
						if (arrayEquals(j.getIndex(), index)) {
							// Found a job in the pending events list which has
							// the same index, remove it and decrement the
							// current count of active jobs
							pendingEvents.remove(e);
							activeJobs--;
							// Now pull any completion events that have reached
							// the head of the queue - this indicates that all
							// the job events which came in before them have
							// been processed and we can emit the completions
							while (pendingEvents.peek() != null
									&& pendingEvents.peek() instanceof Completion) {
								Completion c = (Completion) pendingEvents
										.remove();
								getAbove().receiveResultCompletion(
										new DispatchCompletionEvent(c
												.getOwningProcess(), c
												.getIndex(), c.getContext()));

							}
							// Refresh from the queue; as we've just decremented
							// the active job count there should be a worker
							// available
							fillFromQueue();
							// Return true to indicate that we removed a job
							// event from the queue, that is to say that the
							// index wasn't that of a partial completion.
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean arrayEquals(int[] a, int[] b) {
			if (a.length != b.length) {
				return false;
			}
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
	}

}
