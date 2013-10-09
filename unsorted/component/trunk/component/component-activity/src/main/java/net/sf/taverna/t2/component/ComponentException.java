/**
 * 
 */
package net.sf.taverna.t2.component;

/**
 * @author alanrw
 * 
 */
public class ComponentException extends Exception {

	public ComponentException(String string) {
		super(string);
		this.setStackTrace(new StackTraceElement[] {});
	}

	private static final long serialVersionUID = -3844030382222698090L;
	private String exceptionId;

	public void setExceptionId(String exceptionId) {
		this.exceptionId = exceptionId;
	}

	public String getExceptionId() {
		return exceptionId;
	}

}
