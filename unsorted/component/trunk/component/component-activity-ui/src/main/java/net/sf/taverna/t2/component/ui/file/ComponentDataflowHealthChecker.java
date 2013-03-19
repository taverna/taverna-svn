/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.component.ComponentHealthCheck;
import net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentDataflowHealthChecker implements HealthChecker<Dataflow> {
	
	private static FileManager fm = FileManager.getInstance();
	
	private static Logger logger = Logger.getLogger(ComponentDataflowHealthChecker.class);

	@Override
	public boolean canVisit(Object o) {
		if (!(o instanceof Dataflow)) {
			return false;
		}
		Object source = fm.getDataflowSource((Dataflow) o);
		return (source instanceof ComponentVersionIdentification);
	}

	@Override
	public VisitReport visit(Dataflow dataflow, List<Object> ancestry) {
		
		ComponentVersionIdentification ident = (ComponentVersionIdentification) fm.getDataflowSource(dataflow);
		ComponentFamily family;

			ComponentRegistry registry;
			try {
				registry = ComponentUtil.calculateRegistry(ident.getRegistryBase());
			family = registry.getComponentFamily(ident.getFamilyName());
		Set<SemanticAnnotationProfile> problemProfiles = SemanticAnnotationUtils.checkComponent(dataflow, family.getComponentProfile());
		if (!problemProfiles.isEmpty()) {
			VisitReport visitReport = new VisitReport(ComponentHealthCheck.getInstance(),
						dataflow,
						"Workflow does not satisfy component profile",
						ComponentHealthCheck.FAILS_PROFILE, Status.SEVERE);
			visitReport.setProperty("problemProfiles", problemProfiles);
			return visitReport;
		}
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}

		return null;
	}

	@Override
	public boolean isTimeConsuming() {
		return false;
	}

}
