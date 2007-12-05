package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

public class WSDLHealthReporter {
	private String wsdl;
	private String operationName;
	private WSDLParser parser;

	public WSDLHealthReporter(String wsdl, String operationName,
			WSDLParser parser) {
		this.wsdl = wsdl;
		this.operationName = operationName;
		this.parser = parser;
	}

	private int pingURL(HttpURLConnection httpConnection, int timeout)
			throws IOException {
		httpConnection.setRequestMethod("HEAD");
		httpConnection.connect();
		httpConnection.setReadTimeout(timeout);
		return httpConnection.getResponseCode();
	}

	private HealthReport testWSDL() {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		try {
			URL url = new URL(wsdl);
			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				int code = pingURL((HttpURLConnection) connection, 15000);
				if (code != 200) {
					reports.add(new HealthReportImpl("WSDL Test",
							"Pinging the WSDL did not responded with " + code
									+ " rather than 200", Status.WARNING));
				} else {
					reports.add(new HealthReportImpl("WSDL Test", "The WSDL ["
							+ wsdl + "] repsonded OK", Status.OK));
				}
			}
		} catch (MalformedURLException e) {
			reports.add(new HealthReportImpl("WSDL Test",
					"There was a problem with the WSDL URL:" + e.getMessage(),
					Status.SEVERE));
		} catch (SocketTimeoutException e) {
			reports
					.add(new HealthReportImpl(
							"WSDL Test",
							"Reading the WSDL tool longer than 15 seconds to get a response",
							Status.WARNING));
		} catch (IOException e) {
			reports.add(new HealthReportImpl("WSDL Test",
					"There was an error opening the WSDL:" + e.getMessage(),
					Status.WARNING));
		}
		Status status = highestStatus(reports);
		return new HealthReportImpl("WSDL Tests", wsdl, status, reports);
	}

	private Status highestStatus(List<HealthReport> reports) {
		Status status = Status.OK;
		for (HealthReport report : reports) {
			if (report.getStatus().equals(Status.WARNING)
					&& status.equals(Status.OK))
				status = report.getStatus();
			if (report.getStatus().equals(Status.SEVERE))
				status = Status.SEVERE;
		}
		return status;
	}

	public HealthReport checkHealth() {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		reports.add(testWSDL());
		reports.add(testEndpoint());
		reports.add(testStyleAndUse());

		Status status = highestStatus(reports);
		HealthReportImpl report = new HealthReportImpl("WSDL Activity", "",
				status, reports);
		return report;
	}

	private HealthReport testStyleAndUse() {
		HealthReport report;
		String style = parser.getStyle().toLowerCase();
		String use = "?";
		try {
			use = parser.getUse(operationName).toLowerCase();
			if (use.equals("literal") && style.equals("rpc")) {
				report = new HealthReportImpl("Style and Use",
						"RPC/Literal is not officially supported by Taverna",
						Status.SEVERE);
			}
			else {
				report = new HealthReportImpl ("Style and Use",style+"/"+use +" is OK",Status.OK);
			}
		} catch (UnknownOperationException e) {
			report = new HealthReportImpl("Style and Use",
					"Unable to find use for operation:" + operationName,
					Status.SEVERE);
		}
		return report;
	}

	private HealthReport testEndpoint() {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		List<String> endpoints = parser
				.getOperationEndpointLocations(operationName);
		for (String endpoint : endpoints) {
			URL url;
			try {
				url = new URL(endpoint);

				URLConnection connection = url.openConnection();
				if (connection instanceof HttpURLConnection) {
					int code = pingURL((HttpURLConnection) connection, 15000);
					if (code == 404) {
						reports.add(new HealthReportImpl("Endpoint test","The endpoint ["+endpoint+"] responded, but a response code of "+code,Status.WARNING));
					}
					else {
						reports.add(new HealthReportImpl("Endpoint test","The endpoint ["+endpoint+"] responded, with a response code of "+code,Status.OK));
					}
					
				}
			} catch (MalformedURLException e) {
				reports.add(new HealthReportImpl("Endpoint test","There was a problem with the endpoint["+endpoint+"] URL:"+e.getMessage(),Status.SEVERE));
			} catch (SocketTimeoutException e) {
				reports.add(new HealthReportImpl("Endpoint test","The endpoint["+endpoint+"] took more than 15 seconds to respond",Status.SEVERE));
			} catch (IOException e) {
				reports.add(new HealthReportImpl("Endpoint test","There was an error contacting the endpoint["+endpoint+"]:"+e.getMessage(),Status.SEVERE));
			}
		}

		Status status = highestStatus(reports);
		return new HealthReportImpl("Endpoint tests", "", status, reports);
	}
}
