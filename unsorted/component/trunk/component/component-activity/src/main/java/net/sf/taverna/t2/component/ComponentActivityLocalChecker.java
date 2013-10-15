package net.sf.taverna.t2.component;

import static net.sf.taverna.t2.component.ComponentHealthCheck.NON_SHAREABLE;
import static net.sf.taverna.t2.visit.VisitReport.Status.WARNING;

import java.util.List;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * Component health checker
 * 
 */
public class ComponentActivityLocalChecker implements
		HealthChecker<ComponentActivity> {
	private static final VisitKind visitKind = ComponentHealthCheck
			.getInstance();

	public boolean canVisit(Object o) {
		/*
		 * Return True if we can visit the object. We could do deeper (but not
		 * time consuming) checks here, for instance if the health checker only
		 * deals with ComponentActivity where a certain configuration option is
		 * enabled.
		 */
		return o instanceof ComponentActivity;
	}

	public boolean isTimeConsuming() {
		/*
		 * Return true if the health checker does a network lookup or similar
		 * time consuming checks, in which case it would only be performed when
		 * using File->Validate workflow or File->Run.
		 */
		return false;
	}

	public VisitReport visit(ComponentActivity activity, List<Object> ancestry) {
		if (!activity.getConfiguration().getRegistryBase().getProtocol()
				.startsWith("http")) {
			return new VisitReport(visitKind, activity,
					"Local component makes workflow non-shareable",
					NON_SHAREABLE, WARNING);
		}
		return null;
	}

}
