package net.sf.taverna.t2.cloudone.util;

import net.sf.taverna.t2.cloudone.util.AsynchRunnable;

public abstract class AbstractAsynchRunnable<ResultType> implements
		AsynchRunnable<ResultType> {

	private ResultType result = null;
	private boolean finished = false;
	private Exception exception = null;

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

	public ResultType getResult() {
		if (!finished) {
			throw new IllegalStateException("Not yet finished");
		}
		if (exception != null) {
			throw new IllegalStateException("Invocation failed", exception);
		}
		return result;
	}

	public boolean isFinished() {
		return finished;
	}

	public Exception getException() {
		if (!finished) {
			throw new IllegalStateException("Not yet finished");
		}
		return exception;
	}

}