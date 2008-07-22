package net.sf.taverna.t2.workflowmodel.health;

/**
 * An SPI interface whose implementation performs a health check on an arbitrary instance.
 * <br>
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 * @param <Type> the type of the item being checked
 */
public interface HealthChecker<Type extends Object> {
	/**
	 * Returns true if the HealthChecker implementation is targeted at the subject being
	 * passed to this method.
	 * @param subject
	 * @return
	 */
	public boolean canHandle(Object subject);
	
	/**
	 * Carries out a health check on the subject, which should already have been determined as being
	 * suitable by a call to canHandle.
	 * 
	 * @param subject
	 * @return a HealthReport giving a summary of the result of the health check.
	 * @see HealthReport
	 */
	public HealthReport checkHealth(Type subject);
}
