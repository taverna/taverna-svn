package net.sf.taverna.t2.cloudone.util;

public interface AsynchRunnable<ResultType> extends Runnable{
	
	public ResultType getResult();

	public boolean isFinished();

	public Exception getException();

}
