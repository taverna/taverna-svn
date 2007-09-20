package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.annotation.Annotated;

/**
 * The Activity interface defines a plugin point which can be implemented by
 * third parties. Because the annotated interface is effectively a read only one
 * we can't therefore force those third parties to provide annotation in the
 * correct way unless we make part of the public API mutable which we're trying
 * to avoid doing. This interface therefore defines a bundle of annotation
 * objects (through extension of Annotated) and a single instance of Activity.
 * These are then used in place of the activity list in the original processor
 * interface.
 * 
 * @author Tom Oinn
 * 
 */
public interface ActivityAnnotationContainer extends Annotated {

	/**
	 * Each activity annotation container wraps up a single instance of the
	 * Activity interface
	 * 
	 * @return the activity to which these annotations are applied
	 */
	Activity<?> getActivity();

}
