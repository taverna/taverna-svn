package net.sf.taverna.t2.component;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.dataflow.DataflowActivityHealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * Component health checker
 * 
 */
public class ComponentActivityHealthChecker implements
		HealthChecker<ComponentActivity> {

	public boolean canVisit(Object o) {
		// Return True if we can visit the object. We could do
		// deeper (but not time consuming) checks here, for instance
		// if the health checker only deals with ComponentActivity where
		// a certain configuration option is enabled.
		return o instanceof ComponentActivity;
	}

	public boolean isTimeConsuming() {
		// Return true if the health checker does a network lookup
		// or similar time consuming checks, in which case
		// it would only be performed when using File->Validate workflow
		// or File->Run.
		return false;
	}

	public VisitReport visit(ComponentActivity activity, List<Object> ancestry) {
//		ComponentActivityConfigurationBean config = activity.getConfiguration();
//		
//		DataflowActivityHealthChecker dahc = new DataflowActivityHealthChecker();
//		return dahc.visit(activity.getComponentRealization(), ancestry);
		return null;
	}

}
