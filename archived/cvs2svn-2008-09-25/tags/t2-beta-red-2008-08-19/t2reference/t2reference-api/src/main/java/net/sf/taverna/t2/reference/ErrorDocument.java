package net.sf.taverna.t2.reference;

import java.util.List;
import java.util.Set;

/**
 * Contains the definition of an error token within the workflow system.
 * 
 * @author Tom Oinn
 * @author David Withers
 */
public interface ErrorDocument extends Identified {

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
	
	/**
	 * If the error document is created from set of references that contain error
	 * documents, this method returns them. 
	 */
	public Set<T2Reference> getErrorReferences();

}
