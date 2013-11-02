/**
 * 
 */
package net.sf.taverna.t2.component;

import static net.sf.taverna.t2.component.ComponentHealthCheck.OUT_OF_DATE;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponent;
import static net.sf.taverna.t2.visit.VisitReport.Status.WARNING;

import java.util.List;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentActivityUpgradeChecker implements
		HealthChecker<ComponentActivity> {
	private static final VisitKind visitKind = ComponentHealthCheck
			.getInstance();
	private static Logger logger = Logger
			.getLogger(ComponentActivityUpgradeChecker.class);

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
		int versionNumber = config.getComponentVersion();
		int latestVersion = 0;

		try {
			latestVersion = calculateComponent(config.getRegistryBase(),
					config.getFamilyName(), config.getComponentName())
					.getComponentVersionMap().lastKey();
		} catch (RegistryException e) {
			logger.error("failed to get component description", e);
		}

		if (latestVersion > versionNumber)
			return new VisitReport(visitKind, activity,
					"Component out of date", OUT_OF_DATE, WARNING);
		return null;
	}

}
