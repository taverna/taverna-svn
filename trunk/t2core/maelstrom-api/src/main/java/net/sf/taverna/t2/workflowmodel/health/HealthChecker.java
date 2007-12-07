package net.sf.taverna.t2.workflowmodel.health;

public interface HealthChecker<Type extends Object> {
	public boolean canHandle(Object subject);
	public HealthReport checkHealth(Type subject);
}
