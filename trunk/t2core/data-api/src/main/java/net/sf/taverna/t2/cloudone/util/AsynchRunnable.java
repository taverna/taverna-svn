package net.sf.taverna.t2.cloudone.util;

/**
 * A {@link Runnable} to check whether an event has finished allowing you to get
 * the result if successful or if it has failed get the Exception
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <ResultType>
 *            the result of the runnable
 */
public interface AsynchRunnable<ResultType> extends Runnable {
	/**
	 * What was the result
	 * 
	 * @return the actual result type
	 */
	public ResultType getResult();

	/**
	 * Has the runnable execution ended
	 * 
	 * @return true or false
	 */
	public boolean isFinished();

	/**
	 * If the result has failed then what was the exception trace
	 * 
	 * @return the cause of the failure
	 */
	public Exception getException();

}
