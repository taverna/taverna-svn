package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
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

public class Provenance extends AbstractDispatchLayer<ProvenanceConfig>
		implements NotifiableLayer {

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
		// System.out.println("Provenance layer received error event");

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
		// System.out.println("Provenance layer received job event");
		InvocationContext context = jobEvent.getContext();
		// get something from job event and write to provenance manager?
		// push down
		getBelow().receiveJob(jobEvent);
	}

	@Override
	public void receiveJobQueue(DispatchJobQueueEvent jobQueueEvent) {
		logger.info("Provenance layer received job queue event");
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
		// System.out.println("Provenance layer received result event");
		InvocationContext context = resultEvent.getContext();
		ProvenanceConnector provenanceConnector = context
				.getProvenanceManager();
		provenanceConnector.store(new DataFacade(context.getDataManager()));
		DispatchLayer above = getAbove();
		above.receiveResult(resultEvent);
	}

	@Override
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		logger.info("Provenance layer received completion event");
		InvocationContext context = completionEvent.getContext();
		getAbove().receiveResultCompletion(completionEvent);
	}

	@Override
	public void finishedWith(String owningProcess) {
		// no idea what this is supposed to do but this is what Parallelize does
	}

	public void eventAdded(String owningProcess) {
		// TODO: do something!! not sure what - this is used to tell the layer
		// that something has been added to the queue. Why I am not sure
	}

}
