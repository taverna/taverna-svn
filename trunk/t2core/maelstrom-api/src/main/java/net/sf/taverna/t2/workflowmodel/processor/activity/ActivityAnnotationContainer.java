package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.annotation.Annotated;

/**
 * The Service interface defines a plugin point which can be implemented by
 * third parties. Because the annotated interface is effectively a read only one
 * we can't therefore force those third parties to provide annotation in the
 * correct way unless we make part of the public API mutable which we're trying
 * to avoid doing. This interface therefore defines a bundle of annotation
 * objects (through extension of Annotated) and a single instance of Service.
 * These are then used in place of the service list in the original processor
 * interface.
 * 
 * @author Tom Oinn
 * 
 */
public interface ActivityAnnotationContainer extends Annotated {

	/**
	 * Each service annotation container wraps up a single instance of the
	 * Service interface
	 * 
	 * @return the service to which these annotations are applied
	 */
	Activity<?> getService();

}
