package net.sf.taverna.t2.workflowmodel.health.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

public class HealthCheckerFactory {
	
	private static HealthCheckerFactory instance = new HealthCheckerFactory();
	private HealthCheckerRegistry registry = new HealthCheckerRegistry(); 
	
	private HealthCheckerFactory() {
		
	}
	
	public static HealthCheckerFactory getInstance() {
		return instance;
	}

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
