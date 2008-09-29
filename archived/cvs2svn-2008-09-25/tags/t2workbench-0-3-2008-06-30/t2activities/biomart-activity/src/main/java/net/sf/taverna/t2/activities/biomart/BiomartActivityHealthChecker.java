package net.sf.taverna.t2.activities.biomart;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;
import org.jdom.Element;

public class BiomartActivityHealthChecker implements HealthChecker<BiomartActivity> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof BiomartActivity;
	}

	public HealthReport checkHealth(BiomartActivity activity) {
		Element biomartQueryElement = activity.getConfiguration().getQuery();
		MartQuery biomartQuery = MartServiceXMLHandler.elementToMartQuery(biomartQueryElement, null);
		String location = biomartQuery.getMartService().getLocation();
		Status status = Status.OK;
		String message = "Responded OK";
		try {
			URL url = new URL(location);
			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("HEAD");
				httpConnection.setReadTimeout(10000);
				httpConnection.connect();
				if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					if (httpConnection.getResponseCode() >= 400) {
						status = Status.SEVERE;
					} else {
						status = Status.WARNING;
					}
					message = "Responded with : "
							+ httpConnection.getResponseMessage();
				}
				httpConnection.disconnect();
			}
		} catch (MalformedURLException e) {
			status = Status.SEVERE;
			message = "Location is not a valid URL";
		} catch (SocketTimeoutException e) {
			status = Status.SEVERE;
			message = "Failed to respond within 10s";
		} catch (IOException e) {
			status = Status.SEVERE;
			message = "Error connecting : " + e.getMessage();
		}
		return new HealthReport("Biomart Activity [" + location + "]",
				message, status);
	}

}
