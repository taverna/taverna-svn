package net.sf.taverna.t2.workflowmodel.health;

import java.util.ArrayList;
import java.util.List;


/**
 * A factory class that performs a discovery of available HealthCheckers that can handle a given Object.
 * <br>
 * 
 * 
 * @author Stuart Owen
 * @see HealthReport
 * @see HealthChecker
 */
public class HealthCheckerFactory {
	
	private static HealthCheckerFactory instance = new HealthCheckerFactory();
	private HealthCheckerRegistry registry = new HealthCheckerRegistry(); 
	
	private HealthCheckerFactory() {
		
	}
	
	/**
	 * @return a singleton instance of the HealthCheckerFactory.
	 */
	public static HealthCheckerFactory getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param subject the Object for which to discover HealthCheckers
	 * @return a list of HealthCheckers that can handle the subject.
	 */
	public List<HealthChecker<?>> getHealthCheckersForObject(Object subject) {
		List<HealthChecker<?>> result = new ArrayList<HealthChecker<?>>();
		for (HealthChecker<?> checker : registry.getInstances()) {
			if (checker.canHandle(subject)) {
				result.add(checker);
			}
		}
		return result;
	}
}
