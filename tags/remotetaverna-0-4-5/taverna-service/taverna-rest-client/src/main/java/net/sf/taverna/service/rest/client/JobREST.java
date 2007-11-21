package net.sf.taverna.service.rest.client;

import java.io.IOException;
import java.util.Calendar;

import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.LinkedStatusType;
import net.sf.taverna.service.xml.Report;
import net.sf.taverna.service.xml.StatusType;
import static net.sf.taverna.service.rest.client.RESTContext.xmlOptions;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class JobREST extends OwnedREST<Job> {

	private static Logger logger = Logger.getLogger(JobREST.class);

	public JobREST(RESTContext context, Job job) {
		super(context, job);
	}

	public JobREST(RESTContext context, Reference uri) {
		super(context, uri, Job.class);
	}

	public JobREST(RESTContext context, Reference uri, Job job) {
		super(context, uri, job);
	}

	public WorkflowREST getWorkflow() {
		return new WorkflowREST(context, getDocument().getWorkflow());
	}

	private Reference getStatusURI() {
		String statusURI = getDocument().getStatus().getHref();
		if (statusURI == null) {
			return null;
		}
		return new Reference(getURIReference(), statusURI);
	}

	public StatusType.Enum getStatus() throws RESTException {
		LinkedStatusType status = getCurrentDocument().getStatus();
		if (status == null) {
			status = getDocument().getStatus();
		}
		return (StatusType.Enum) status.enumValue();
	}


	public void setStatus(StatusType.Enum status) throws NotSuccessException {
		Reference statusURI = getStatusURI();
		if (statusURI != null) {
			context.put(statusURI, status.toString(), MediaType.TEXT_PLAIN);
		} else {
			JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
			job.addNewJob().addNewStatus().set(status);
			context.put(getURIReference(), job);
			invalidate();
		}
	}

	public DataREST getInputs() {		
		Data inputs = getCurrentDocument().getInputs();
		if (inputs == null) {
			inputs = getDocument().getInputs();
		}
		if (inputs == null) {
			return null;
		}
		return new DataREST(context, inputs);
	}

	public DataREST getOutputs() {
		Data outputs = getCurrentDocument().getOutputs();
		if (outputs == null) {
			outputs = getDocument().getOutputs();
		}
		if (outputs == null) {
			return null;
		}
		return new DataREST(context, outputs);
	}

	public void setOutputs(DataREST rest) throws NotSuccessException {
		JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
		job.addNewJob().addNewOutputs().setHref(rest.getURI());
		context.put(getURIReference(), job);
		invalidate();
	}

	private Reference getReportURI() {
		Report report = getCurrentDocument().getReport();
		if (report == null) {
			report = getDocument().getReport();
		}
		String reportURI = report.getHref();
		if (reportURI == null) {
			return null;
		}
		return getRelativeReference(reportURI);
	}


	public String getReport() throws RESTException {
		Report report = getCurrentDocument().getReport();
		if (report == null) {
			report = getDocument().getReport();
		}
		if (report == null) {
			return null;
		}
		String uri = report.getHref();
		if (uri == null) {
			return report.xmlText();
		}
		return getString(uri, RESTContext.reportType);
	}

	public void setReport(String report) throws NotSuccessException,
		XmlException {
		if (report == null) {
			report = "";
		}
		Reference reportURI = getReportURI();
		if (reportURI != null) {
			context.put(reportURI, report, RESTContext.reportType);
		} else {
			JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
			job.addNewJob().addNewReport(); // empty
			if (!report.equals("")) {
				XmlObject reportXML = XmlObject.Factory.parse(report);
				job.getJob().getReport().set(reportXML);
				logger.info("Setting report to:\n" + reportXML);
			}
			context.put(getURIReference(), job);
		}
		invalidate();
	}

	public GDuration getUpdateInterval() {
		return getDocument().getUpdateInterval();
	}

	public void setUpdateInterval(GDuration interval)
		throws NotSuccessException {
		JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
		job.addNewJob().setUpdateInterval(interval);
		context.put(getURIReference(), job);
		invalidate();
	}
	
	private Reference getConsoleURI() {
		if (getDocument().getConsole() == null) {
			return null;
		}
		String reportURI = getDocument().getConsole().getHref();
		if (reportURI == null) {
			return null;
		}
		return getRelativeReference(reportURI);
	}
	
	public String getConsole() throws RESTException {
		if (getDocument().getConsole() == null) {
			return null;
		}
		Reference uri = getConsoleURI();
		if (uri == null) {
			return getDocument().getConsole().getStringValue();
		} else {
			return getString(uri, RESTContext.consoleType);
		}
	}
	
	public void setConsole(String console) throws NotSuccessException {
		Reference uri = getConsoleURI();
		if (uri != null) {
			context.put(uri, console, RESTContext.consoleType);
		} else {
			JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
			job.addNewJob().addNewConsole().setStringValue(console);
			context.put(getURIReference(), job);
		}
		invalidate();
	}
	
	public JobREST clone() {
		return new JobREST(context, getURIReference());
	}



}
