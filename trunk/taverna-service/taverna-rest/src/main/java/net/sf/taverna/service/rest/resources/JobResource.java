package net.sf.taverna.service.rest.resources;

import java.util.Date;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.XMLUtils;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class JobResource extends AbstractUserResource {

	private static Logger logger = Logger.getLogger(JobResource.class);
	
	private Job job;

	public JobResource(Context context, Request request, Response response) {
		super(context, request, response);
		JobDAO dao = daoFactory.getJobDAO();
		String job_id = (String) request.getAttributes().get("job");
		job = dao.read(job_id);
		checkEntity(job);
	}

	@Override
	public Date getModificationDate() {
		return job.getLastModified();
	}
	
	@Override
	public String representPlainText() {
		StringBuilder sb = new StringBuilder();
		sb.append("Job ").append(job.getId()).append('\n');
		sb.append("Created: ").append(job.getCreated()).append('\n');
		sb.append("Last-Modified: ").append(job.getLastModified()).append('\n');
		sb.append("State: ").append(job.getState()).append('\n');
		
		sb.append("Workflow: ").append(uriFactory.getURI(job.getWorkflow())).append('\n');
		if (job.getInputs() != null) {
			sb.append("Inputs: ").append(uriFactory.getURI(job.getInputs())).append('\n');
		}
		if (job.getResultDoc() != null) {
			sb.append("Outputs: ").append(uriFactory.getURI(job.getResultDoc())).append('\n');
		}
		if (job.getProgressReport() != null) {
			sb.append("Progress: ").append(uriFactory.getURI(job)).append("progress").append('\n');
		}

		return sb.toString();
	}

	@Override
	public Element representXMLElement() {
		Element jobElement = new Element("job", ns);
		jobElement.setAttribute("id", job.getId());
		
		Element stateElement = new Element("state", ns);
		jobElement.addContent(stateElement);
		stateElement.setAttribute("href", uriFactory.getURI(job) + "/state",
			URIFactory.NS_XLINK);
		stateElement.addContent(job.getState().toString());
		
		Element wfElement = new Element("workflow", ns);
		jobElement.addContent(wfElement);
		wfElement.setAttribute(uriFactory.getXLink(job.getWorkflow()));
	
		DataDoc inputs = job.getInputs();
		if (inputs != null) {
			Element inputElement = new Element("inputs", ns);
			jobElement.addContent(inputElement);
			inputElement.setAttribute(uriFactory.getXLink(inputs));
		}
		
		DataDoc outputs = job.getResultDoc();
		if (outputs != null) {
			Element outputElement = new Element("outputs", ns);
			jobElement.addContent(outputElement);
			outputElement.setAttribute(uriFactory.getXLink(outputs));
		}
		
		try {
			Document progressReport = XMLUtils.parseXML(job.getProgressReport());
			Element progressElement = progressReport.getRootElement();
			stateElement.setAttribute("href", uriFactory.getURI(job) + "/progress",
				URIFactory.NS_XLINK);
			wfElement.addContent(progressElement);
		} catch (ParseException e) {
			logger.warn("Invalid progress report for " + job, e);
		}
		
		return jobElement;
	}
}
