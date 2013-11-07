package net.sf.taverna.t2.component;

import static net.sf.taverna.t2.visit.VisitReport.findAncestor;
import static org.apache.log4j.Logger.getLogger;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;

import org.apache.log4j.Logger;

public class DispatchStackPatcher implements HealthChecker<ComponentActivity> {

	private static Logger logger = getLogger(DispatchStackPatcher.class);

	@Override
	public boolean canVisit(Object o) {
		return false;
		// return o instanceof ComponentActivity;
	}

	@Override
	public boolean isTimeConsuming() {
		return false;
	}

	@Override
	public VisitReport visit(ComponentActivity a, List<Object> ancestry) {
		Processor p = (Processor) findAncestor(ancestry, Processor.class);
		DispatchStackImpl ds = (DispatchStackImpl) p.getDispatchStack();
		List<DispatchLayer<?>> layers = ds.getLayers();
		DispatchLayer<?> oldLayer = null;
		for (DispatchLayer<?> dl : layers)
			if ((dl instanceof Invoke) && !(dl instanceof PatchedInvoke))
				oldLayer = dl;
		try {
			if (oldLayer != null) {
				int oldIndex = ds.removeLayer(oldLayer);

				ds.addLayer(new PatchedInvoke(), oldIndex);
			}
		} catch (Exception e) {
			logger.error("failed to patch invoke layer", e);
		}
		return null;
	}

}
