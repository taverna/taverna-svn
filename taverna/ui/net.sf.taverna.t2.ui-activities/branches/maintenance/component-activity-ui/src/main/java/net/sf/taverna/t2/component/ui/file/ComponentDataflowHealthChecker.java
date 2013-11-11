/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import static net.sf.taverna.t2.component.ComponentHealthCheck.FAILS_PROFILE;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.checkComponent;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateFamily;
import static net.sf.taverna.t2.visit.VisitReport.Status.SEVERE;
import static org.apache.log4j.Logger.getLogger;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.component.ComponentHealthCheck;
import net.sf.taverna.t2.component.api.Family;
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
	private static final String PROFILE_UNSATISFIED_MSG = "Workflow does not satisfy component profile";
	private static FileManager fm = FileManager.getInstance();
	private static Logger logger = getLogger(ComponentDataflowHealthChecker.class);
	private static ComponentHealthCheck visitType = ComponentHealthCheck
			.getInstance();

	private Version.ID getSource(Object o) {
		return (Version.ID) fm.getDataflowSource((Dataflow) o);
	}

	@Override
	public boolean canVisit(Object o) {
		try {
			return (getSource(o) != null);
		} catch (IllegalArgumentException e) {
			// Not open?
		} catch (ClassCastException e) {
			// Not dataflow? Not component?
		}
		return false;
	}

	@Override
	public VisitReport visit(Dataflow dataflow, List<Object> ancestry) {
		try {
			Version.ID ident = getSource(dataflow);
			Family family = calculateFamily(ident.getRegistryBase(),
					ident.getFamilyName());

			Set<SemanticAnnotationProfile> problemProfiles = checkComponent(
					dataflow, family.getComponentProfile());
			if (problemProfiles.isEmpty())
				return null;

			VisitReport visitReport = new VisitReport(visitType, dataflow,
					PROFILE_UNSATISFIED_MSG, FAILS_PROFILE, SEVERE);
			visitReport.setProperty("problemProfiles", problemProfiles);
			return visitReport;
		} catch (RegistryException e) {
			logger.error(
					"failed to comprehend profile while checking for match", e);
			return null;
		}
	}

	@Override
	public boolean isTimeConsuming() {
		return false;
	}
}
