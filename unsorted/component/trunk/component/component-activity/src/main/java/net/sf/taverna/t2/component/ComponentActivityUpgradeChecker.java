/**
 * 
 */
package net.sf.taverna.t2.component;

import java.util.List;

import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentActivityUpgradeChecker implements HealthChecker<ComponentActivity> {
	
	private static Logger logger = Logger.getLogger(ComponentActivityUpgradeChecker.class);


	@Override
	public boolean canVisit(Object o) {
		return o instanceof ComponentActivity;
	}

	@Override
	public boolean isTimeConsuming() {
		return false;
	}

	@Override
	public VisitReport visit(ComponentActivity activity, List<Object> ancestry) {
		ComponentActivityConfigurationBean config = activity.getConfiguration();
		
		Integer versionNumber = config.getComponentVersion();
		
		Integer latestVersion = 0;
		try {
			latestVersion = ComponentUtil.calculateComponent(config.getRegistryBase(), config.getFamilyName(), config.getComponentName()).getComponentVersionMap().lastKey();
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
		
		if (latestVersion > versionNumber) {
			return new VisitReport(ComponentHealthCheck.getInstance(), activity, 
                    "Component out of date", 
                    ComponentHealthCheck.OUT_OF_DATE, Status.WARNING);
		}
		return null;
	}

}
