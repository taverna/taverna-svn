package net.sf.taverna.t2.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.dataflow.DataflowActivityHealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;

public class DispatchStackPatcher implements
		HealthChecker<Processor> {
	
	private static Logger logger = Logger.getLogger(DispatchStackPatcher.class);
	


	public boolean canVisit(Object o) {
		return o instanceof Processor;
	}

	public boolean isTimeConsuming() {
	return false;
	}

	public VisitReport visit(Processor p, List<Object> ancestry) {
		DispatchStackImpl ds = (DispatchStackImpl) p.getDispatchStack();
		List<DispatchLayer<?>> layers = ds.getLayers();
		DispatchLayer oldLayer = null;
		for (DispatchLayer<?> dl : layers) {
			if ((dl instanceof Invoke) && !(dl instanceof PatchedInvoke)){
				oldLayer = dl;
			}
		}
		try {
		if (oldLayer != null) {
			ds.removeLayer(oldLayer);

			ds.addLayer(new PatchedInvoke());
		}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

}
