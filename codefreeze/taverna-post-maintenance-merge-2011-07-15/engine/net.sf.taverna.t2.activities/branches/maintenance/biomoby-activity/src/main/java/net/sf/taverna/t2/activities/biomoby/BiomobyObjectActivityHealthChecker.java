/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;

import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;

/**
 * A health checker for the Biomoby Object activity.
 * 
 * @author David Withers
 */
public class BiomobyObjectActivityHealthChecker extends RemoteHealthChecker {
	
	public boolean canVisit(Object subject) {
		if (subject == null) {
			return false;
		}
		if (subject instanceof BiomobyObjectActivity) {
			return true;
		}
		if (subject instanceof DisabledActivity) {
			return (((DisabledActivity) subject).getActivity() instanceof BiomobyObjectActivity);
		}
		return false;
	}

	public VisitReport visit(Object o, List<Object> ancestors) {
		Activity activity = (Activity) o;
		BiomobyObjectActivityConfigurationBean configuration = null;
		if (activity instanceof BiomobyObjectActivity) {
			configuration = (BiomobyObjectActivityConfigurationBean) activity.getConfiguration();
		} else if (activity instanceof DisabledActivity) {
			configuration = (BiomobyObjectActivityConfigurationBean) ((DisabledActivity) activity).getActivityConfiguration();
		}
		return contactEndpoint(activity, configuration.getMobyEndpoint());

	}

}
