package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.Job;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class JobsResource extends AbstractUserResource {
	
	private static Logger logger = Logger.getLogger(JobsResource.class);
	
	public JobsResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public String representPlainText() {
    	StringBuilder message = new StringBuilder();
    	for (Job job : user.getJobs()) {
    		message.append(uriFactory.getURI(job)).append("\n");
    	}
    	return message.toString();
	}

	@Override
	public Element representXMLElement() {
		Element jobsElement = new Element("jobs", ns);
		for (Job job : user.getJobs()) {
			Element jobElement = new Element("job", ns);
			jobElement.setAttribute(uriFactory.getXLink(job));
			jobsElement.addContent(jobElement);
		}
		return jobsElement;
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public void post(Representation entity) {
		if (! entity.getMediaType().equals(restType)) {
			System.out.println("Wrong type: " + entity.getMediaType());
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, 
				"Content type must be " + restType);
			return;
		}
		SAXBuilder builder = new SAXBuilder();		
		Document doc;
		try {
			try {
				doc = builder.build(entity.getStream());
			} catch (JDOMException e) {
				logger.warn("Could not parse job document", e);
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse as XML");
				return;
			}
		} catch (IOException e) {
			logger.warn("Could not read XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Could not read XML");
			return;
		}
		Job job = new Job();
		job.setOwner(user);		
		
		Element jobElement = doc.getRootElement();
		Element status = jobElement.getChild("status", ns);
		System.out.println(status.getTextNormalize());
	}
	
}
