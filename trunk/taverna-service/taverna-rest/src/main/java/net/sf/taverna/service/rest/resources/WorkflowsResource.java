package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class WorkflowsResource extends AbstractUserResource {
	
	private static Logger logger = Logger.getLogger(WorkflowsResource.class);
	
	private WorkflowDAO wfDao = daoFactory.getWorkflowDAO();
	
	public WorkflowsResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(scuflType));
	}

	@Override
	public String representPlainText() {
    	StringBuilder message = new StringBuilder();
    	for (Workflow wf : user.getWorkflows()) {
    		message.append(wf.getId()).append("\n");
    	}
    	return message.toString();
	}

	@Override
	public Element representXMLElement() {
		Element jobsElement = new Element("workflows", ns);
		jobsElement.addNamespaceDeclaration(URIFactory.NS_XLINK);
		for (Workflow wf : user.getWorkflows()) {
			Element wfElement = new Element("workflow", ns);
			wfElement.setAttribute(uriFactory.getXLink(wf));
			jobsElement.addContent(wfElement);
		}
		return jobsElement;
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public long maxSize() {
		return Workflow.SCUFL_MAX;
	}
	
	@Override
	public void post(Representation entity) {
		if (! restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, 
				"Content type must be " + restType);
			return;
		}
		if (overMaxSize(entity)) {
			logger.warn("Uploaded scufl was too large");
			return;
		}
		Workflow wf = new Workflow();
		try {
			wf.setScufl(entity.getText());
		} catch (IOException e) {
			logger.warn("Could not receive Scufl", e);
		}
		wfDao.create(wf);
		daoFactory.commit();
		getResponse().setRedirectRef("/workflows/" + wf.getId());
		getResponse().setStatus(Status.SUCCESS_CREATED);
		
	}
	
}
