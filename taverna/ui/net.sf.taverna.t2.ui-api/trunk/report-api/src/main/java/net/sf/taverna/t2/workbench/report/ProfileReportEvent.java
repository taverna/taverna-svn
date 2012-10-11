/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.profiles.Profile;

//import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ProfileReportEvent implements ReportManagerEvent {

	private final Profile profile;

	public ProfileReportEvent(Profile d) {
		this.profile = d;
	}

	public Profile getProfile() {
		return profile;
	}

}
