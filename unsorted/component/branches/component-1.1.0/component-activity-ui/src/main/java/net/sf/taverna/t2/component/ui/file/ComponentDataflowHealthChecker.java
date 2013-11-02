/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import static net.sf.taverna.t2.component.ComponentHealthCheck.FAILS_PROFILE;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.checkComponent;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static net.sf.taverna.t2.visit.VisitReport.Status.SEVERE;
import static org.apache.log4j.Logger.getLogger;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.component.ComponentHealthCheck;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.visit.VisitReport;
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
	private static Logger logger = getLogger(ComponentDataflowHealthChecker.class);

	@Override
	public boolean canVisit(Object o) {
		if (!(o instanceof Dataflow))
			return false;
		Object source = fm.getDataflowSource((Dataflow) o);
		return (source instanceof Version.ID);
	}

	@Override
	public VisitReport visit(Dataflow dataflow, List<Object> ancestry) {
		Version.ID ident = (Version.ID) fm.getDataflowSource(dataflow);
		Family family;

		Registry registry;
		try {
			registry = calculateRegistry(ident.getRegistryBase());
			family = registry.getComponentFamily(ident.getFamilyName());
			Set<SemanticAnnotationProfile> problemProfiles = checkComponent(
					dataflow, family.getComponentProfile());
			if (!problemProfiles.isEmpty()) {
				VisitReport visitReport = new VisitReport(
						ComponentHealthCheck.getInstance(), dataflow,
						"Workflow does not satisfy component profile",
						FAILS_PROFILE, SEVERE);
				visitReport.setProperty("problemProfiles", problemProfiles);
				return visitReport;
			}
		} catch (RegistryException e) {
			logger.error(
					"failed to comprehend profile while checking for match", e);
		}

		return null;
	}

	@Override
	public boolean isTimeConsuming() {
		return false;
	}
}
