package net.sf.taverna.t2.workflowmodel.health.impl;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

public class HealthCheckerFactory {
	
	private static HealthCheckerFactory instance = new HealthCheckerFactory();
	private HealthCheckerRegistry registry = new HealthCheckerRegistry(); 
	
	private HealthCheckerFactory() {
		
	}
	
	public static HealthCheckerFactory getInstance() {
		return instance;
	}

	public HealthChecker<?> getHealthCheckerForObject(Object subject) {
		for (HealthChecker<?> checker : registry.getInstances()) {
			if (checker.canHandle(subject)) {
				return checker;
			}
		}
		return null;
	}
}
