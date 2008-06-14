package net.sf.taverna.t2.reference;

/**
 * Used by the {@link ErrorDocument} interface to represent a frame within a
 * stack trace
 * 
 * @author Tom Oinn
 * 
 */
public interface StackTraceElementBean {

	/**
	 * Returns the fully qualified name of the class containing the execution
	 * point represented by this stack trace element.
	 */
	public String getClassName();

	/**
	 * Returns the name of the source file containing the execution point
	 * represented by this stack trace element.
	 */
	public String getFileName();

	/**
	 * Returns the line number of the source line containing the execution point
	 * represented by this stack trace element.
	 */
	public int getLineNumber();

	/**
	 * Returns the name of the method containing the execution point represented
	 * by this stack trace element.
	 */
	public String getMethodName();

}