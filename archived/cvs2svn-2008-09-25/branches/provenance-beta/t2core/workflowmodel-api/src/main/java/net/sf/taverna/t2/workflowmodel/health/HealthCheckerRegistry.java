package net.sf.taverna.t2.workflowmodel.health;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI registry class for the discovery of known HealthCheckers.
 * <br>
 * This class is not normally used directly. To find a HealthChecker for a given Object then {@link HealthCheckerFactory} should be used instead.
 * 
 * @author Stuart Owen
 * @see HealthChecker
 * @see HealthReport
 */
@SuppressWarnings("unchecked")
public class HealthCheckerRegistry extends SPIRegistry<HealthChecker>{

	public HealthCheckerRegistry() {
		super(HealthChecker.class);
	}

}
