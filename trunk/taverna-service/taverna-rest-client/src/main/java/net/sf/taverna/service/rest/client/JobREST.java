package net.sf.taverna.service.rest.client;

import java.io.IOException;

import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.StatusType;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;

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
		Reference statusURI = getStatusURI();
		if (statusURI == null) {
			// no link for live-update, return what we have
			return (StatusType.Enum) getDocument().getStatus().enumValue();
		}
		// Return the freshest of the freshest status with a new get()
		Response response = context.get(statusURI, MediaType.TEXT_PLAIN);
		String status;
		try {
			status = response.getEntity().getText();
		} catch (IOException e) {
			throw new RESTException("Could not receive status", e);
		}
		logger.debug("Status for " + this + ": " + status);
		return StatusType.Enum.forString(status);
	}

	public void setStatus(StatusType.Enum status) throws NotSuccessException {
		Reference statusURI = getStatusURI();
		if (statusURI != null) {
			context.put(statusURI, status.toString(), MediaType.TEXT_PLAIN);
		} else {
			JobDocument job = JobDocument.Factory.newInstance();
			job.addNewJob().addNewStatus().set(status);
			context.put(getURIReference(), job);
		}
	}
	
	public DataREST getInputs() {
		return new DataREST(context, getDocument().getInputs());
	}
	
	public DataREST getOutputs() {
		return new DataREST(context, getDocument().getOutputs());
	}
	
	public void setOutputs(DataREST rest) throws NotSuccessException {
		Job job = Job.Factory.newInstance();
		job.addNewOutputs().setHref(rest.getURI());
		context.put(getURIReference(), job);
	}
	
	private Reference getReportURI() {
		String reportURI = getDocument().getReport().getHref();
		if (reportURI == null) { 
			return null;
		}
		return new Reference(getURIReference(), reportURI);
	}
	
	public String getReport() throws RESTException {
		Reference reportURI = getReportURI();
		if (reportURI == null) {
			return getDocument().getReport().xmlText();
		}
		
		// Return the freshest report with a new get()
		Response response = context.get(reportURI, MediaType.TEXT_PLAIN);
		try {
			return response.getEntity().getText();
		} catch (IOException e) {
			throw new RESTException("Could not receive report " + reportURI, e);
		}
	}

	public void setReport(String report) throws NotSuccessException, XmlException {
		Reference reportURI = getReportURI();
		if (reportURI != null) {
			context.put(reportURI, report, MediaType.TEXT_XML);
		} else {
			JobDocument job = JobDocument.Factory.newInstance();
			XmlObject reportXML = XmlObject.Factory.parse(report);
			job.addNewJob().addNewReport().set(reportXML);
			context.put(getURIReference(), job);
		}
	}
	

}
