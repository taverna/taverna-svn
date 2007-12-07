package net.sf.taverna.t2.workflowmodel.health.impl;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

public class HealthCheckerRegistry extends SPIRegistry<HealthChecker>{

	public HealthCheckerRegistry() {
		super(HealthChecker.class);
	}

}
