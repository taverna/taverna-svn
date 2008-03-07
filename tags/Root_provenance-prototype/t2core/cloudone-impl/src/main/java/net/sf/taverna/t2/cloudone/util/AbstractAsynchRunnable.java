package net.sf.taverna.t2.cloudone.util;

import net.sf.taverna.t2.cloudone.util.AsynchRunnable;

/**
 * Generic Asynchronous Runnable parameterised on the return type
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <ResultType>
 *            Return type
 */
public abstract class AbstractAsynchRunnable<ResultType> implements
		AsynchRunnable<ResultType> {

	private ResultType result = null;
	private boolean finished = false;
	private Exception exception = null;

	/**
	 * Check if it has been run before, if not run it via {@link #execute()}
	 */
	public final void run() {
		if (finished) {
			throw new IllegalStateException("Can't run twice");
		}
		try {
			ResultType result = execute();
			setResult(result);
		} catch (Exception e) {
			exception = e;
		} finally {
			finished = true;
		}
	}

	protected abstract ResultType execute() throws Exception;

	private void setResult(ResultType result) {
		this.result = result;
	}

	/**
	 * Returns the result if the runnable has finished without failure,
	 * otherwise throws an exception that it is still executing
	 */
	public ResultType getResult() {
		if (!finished) {
			throw new IllegalStateException("Not yet finished");
		}
		if (exception != null) {
			throw new IllegalStateException("Invocation failed", exception);
		}
		return result;
	}

	/**
	 * Has the runnable finished or not
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * If the runnable failed then why
	 */
	public Exception getException() {
		if (!finished) {
			throw new IllegalStateException("Not yet finished");
		}
		return exception;
	}

}