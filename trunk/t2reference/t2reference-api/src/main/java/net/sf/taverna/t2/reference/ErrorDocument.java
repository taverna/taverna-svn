package net.sf.taverna.t2.reference;

import java.util.List;

/**
 * Contains the definition of an error token within the workflow system.
 * 
 * @author Tom Oinn
 * 
 */
public interface ErrorDocument {

	/**
	 * Each error document has a unique T2Reference
	 */
	public T2Reference getId();

	/**
	 * If the error document is created from a Throwable it will have a stack
	 * trace, in this case the stack trace is represented as a list of
	 * StackTraceElement beans
	 */
	public List<StackTraceElementBean> getStackTraceStrings();

	/**
	 * If the error document is created from a Throwable this contains the
	 * message part of the Throwable
	 */
	public String getExceptionMessage();

	/**
	 * Error documents can carry an arbitrary string message, this returns it.
	 */
	public String getMessage();

}
