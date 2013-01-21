package uk.ac.soton.mib104.t2.activities.oauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.ac.soton.mib104.t2.activities.oauth.util.HttpVerb;
import uk.ac.soton.mib104.t2.activities.oauth.util.Format;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
//import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * OAuthActivityHealthChecker is a T2 health-checker for instances of the OAuthActivity class.
 * 
 * @author Mark Borkum
 * 
 * @see OAuthActivity
 * @see OAuthActivityConfigurationBean
 */
public class OAuthActivityHealthChecker implements HealthChecker<OAuthActivity> {

	@Override
	public boolean canVisit(final Object obj) {
		return (obj != null) && (obj instanceof OAuthActivity);
	}
	
	@Override
	public boolean isTimeConsuming() {
		return false;
	}
	
	@Override
	public VisitReport visit(final OAuthActivity activity, final List<Object> ancestry) {
//		final Processor processor = (Processor) VisitReport.findAncestor(ancestry, Processor.class);
//		
//		if (processor == null) {
//			return null;
//		}
		
		final List<VisitReport> reports = new ArrayList<VisitReport>();

		final OAuthActivityConfigurationBean configBean = activity.getConfiguration();
		
		if (configBean == null) {
			reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: no configuration bean", HealthCheck.NO_CONFIGURATION, Status.SEVERE));
		} else {
			if (configBean.isRequestTokenRequired()) {
				{
					final String requestTokenEndpoint = configBean.getRequestTokenEndpoint();
					
					if (requestTokenEndpoint == null) {
						reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Request token end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
					} else {
						try {
							new URL(requestTokenEndpoint);
						} catch (final MalformedURLException ex) {
							reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Request token end-point is invalid", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE));
						}
					}
				}
				
				{
					final HttpVerb requestTokenVerb = configBean.getRequestTokenVerb();
					
					if (requestTokenVerb == null) {
						reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Verb for request token end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
					}
				}
			}
			
			{
				final String authorizationEndpoint = configBean.getAuthorizationEndpoint();
				
				if (authorizationEndpoint == null) {
					reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Authorization end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
				} else {
					try {
						new URL(authorizationEndpoint);
					} catch (final MalformedURLException ex) {
						reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Authorization end-point is invalid", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE));
					}
				}
			}
			
			{
				final String accessTokenEndpoint = configBean.getAccessTokenEndpoint();
				
				if (accessTokenEndpoint == null) {
					reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Access token end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
				} else {
					try {
						new URL(accessTokenEndpoint);
					} catch (final MalformedURLException ex) {
						reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Access token end-point is invalid", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE));
					}
				}
			}
			
			{
				final Format accessTokenFormat = configBean.getAccessTokenFormat();
				
				if (accessTokenFormat == null) {
					reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Format for access token end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
				}
			}
			
			{
				final HttpVerb accessTokenVerb = configBean.getAccessTokenVerb();
				
				if (accessTokenVerb == null) {
					reports.add(new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity error: Verb for access token end-point is unspecified", HealthCheck.NULL_VALUE, Status.SEVERE));
				}
			}
		}
		
		if (reports.isEmpty()) {
			return new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity is OK", HealthCheck.NO_PROBLEM, Status.OK);
		} else {
			final Status worstStatus = VisitReport.getWorstStatus(reports);
			
			return new VisitReport(HealthCheck.getInstance(), activity, "OAuthActivity contains errors", HealthCheck.INVALID_CONFIGURATION, worstStatus, reports);
		}
	}

}
