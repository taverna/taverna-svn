package net.sf.taverna.t2.workflowmodel.processor.iteration;

/**
 * Thrown during the typecheck phase when an iteration strategy is configured
 * such that at runtime it would fail. This is generally because a dot product
 * node has been specified where the children of that node will have different
 * cardinalities (in this case the dot product isn't defined)
 * 
 * @author Tom Oinn
 * 
 */
public class IterationTypeMismatchException extends Exception {

	private static final long serialVersionUID = -3034020607723767223L;

	public IterationTypeMismatchException() {
		// TODO Auto-generated constructor stub
	}

	public IterationTypeMismatchException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public IterationTypeMismatchException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public IterationTypeMismatchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
