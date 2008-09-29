package net.sf.taverna.t2.activities.soaplab;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class SoaplabActivityHealthChecker implements HealthChecker<SoaplabActivity> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof SoaplabActivity;
	}

	public HealthReport checkHealth(SoaplabActivity activity) {
		return testEndpoint(activity);
	}

	private int pingURL(HttpURLConnection httpConnection, int timeout)
			throws IOException {
		httpConnection.setRequestMethod("HEAD");
		httpConnection.connect();
		httpConnection.setReadTimeout(timeout);
		return httpConnection.getResponseCode();
	}

	private HealthReport testEndpoint(SoaplabActivity activity) {
		HealthReport report;
		String endpoint = activity.getConfiguration().getEndpoint();

		try {
			URL url = new URL(endpoint);
			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				int code = pingURL((HttpURLConnection) connection, 15000);
				if (code == 200) {
					report = new HealthReport("SOAPLab Activity",
							"The endpoint [" + endpoint
									+ "] responded with a response code of "
									+ code, Status.OK);

				} else {
					report = new HealthReport("SOAPLab Activity",
							"The endpoint [" + endpoint
									+ "] responded, but a response code of "
									+ code, Status.WARNING);
				}
			}
			else {
				return new HealthReport("SOAPLab Activity","The endpoint["+endpoint+"] is not Http based and could not be tested for a http response",Status.OK);
			}
		} catch (MalformedURLException e) {
			report = new HealthReport("SOAPLab Activity",
					"There was a problem with the endpoint[" + endpoint
							+ "] URL:" + e.getMessage(), Status.SEVERE);
		} catch (SocketTimeoutException e) {
			report = new HealthReport("SOAPLab Activity", "The endpoint["
					+ endpoint + "] took more than 15 seconds to respond",
					Status.SEVERE);
		} catch (IOException e) {
			report = new HealthReport("SOAPLab Activity",
					"There was an error contacting the endpoint[" + endpoint
							+ "]:" + e.getMessage(), Status.SEVERE);
		}

		return report;
	}
}
