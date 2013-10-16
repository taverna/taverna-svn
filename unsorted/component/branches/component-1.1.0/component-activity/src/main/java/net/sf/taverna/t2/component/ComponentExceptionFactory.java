/**
 * 
 */
package net.sf.taverna.t2.component;

/**
 * @author alanrw
 * 
 */
public class ComponentExceptionFactory {

	private static final String UNEXPECTED_ID = "http://ns.taverna.org.uk/2012/component/unexpected";

	private ComponentExceptionFactory() {
	}

	public static ComponentException createComponentException(
			String exceptionId, String message) {
		ComponentException result = new ComponentException(message);
		result.setExceptionId(exceptionId);
		return result;
	}

	public static ComponentException createUnexpectedComponentException(
			String message) {
		return createComponentException(UNEXPECTED_ID, message);
	}

}
