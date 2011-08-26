package net.sf.taverna.t2.workflowmodel;

/**
 * Thrown predominantly at runtime under circumstances that suggest an
 * inconsistancy in the workflow model. This might include attempting to feed
 * data into a port that doesn't exist or has an unknown name or similar errors.
 * 
 * @author Tom OInn
 */
public class WorkflowStructureException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkflowStructureException(String string) {
		super(string);
	}

}
