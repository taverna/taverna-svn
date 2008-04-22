package net.sf.taverna.t2.workflowmodel.processor.iteration;

/**
 * Thrown when an attempt is made to evaluate the type of the iteration strategy
 * but one or more input ports aren't defined in the input array of types.
 * Shouldn't normally happen as this will be handled by the type checker
 * detecting that there aren't enough inputs to check but we indicate it for
 * extra robustness.
 * 
 * @author Tom Oinn
 * 
 */
public class MissingIterationInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1615949178096496592L;

	public MissingIterationInputException() {
		// TODO Auto-generated constructor stub
	}

	public MissingIterationInputException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public MissingIterationInputException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public MissingIterationInputException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
