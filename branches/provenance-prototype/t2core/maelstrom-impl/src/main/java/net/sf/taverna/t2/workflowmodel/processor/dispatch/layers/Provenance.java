package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
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
		getAbove().receiveError(errorEvent);
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
	
		getBelow().receiveJobQueue(jobQueueEvent);
	}

	/**
	 * Receive results from layer below in the dispatch stack. Create an XML
	 * representation of the results and send to the {@link ProvenanceConnector}
	 * in the {@link DispatchStack}
	 */
	@Override
	public void receiveResult(DispatchResultEvent resultEvent) {
		logger.info("Provenance layer received result event");
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
		DispatchLayer<?> above = getAbove();
		above.receiveResult(resultEvent);
	}

	@Override
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		logger.info("Provenance layer received completion event");
		getAbove().receiveResultCompletion(completionEvent);
	}

	@Override
	public void finishedWith(String owningProcess) {
		
	}

	public void eventAdded(String owningProcess) {
		
	}
}
