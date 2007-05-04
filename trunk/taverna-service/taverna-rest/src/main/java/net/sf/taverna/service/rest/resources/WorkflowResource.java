package net.sf.taverna.service.rest.resources;

import java.util.Date;

import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.xml.Scufl;
import net.sf.taverna.service.xml.WorkflowDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlString;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class WorkflowResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(WorkflowResource.class);
	
	private Workflow workflow;

	private static WorkflowDAO dao = daoFactory.getWorkflowDAO();

	public WorkflowResource(Context context, Request request, Response response) {
		super(context, request, response);
		String wf_id = (String) request.getAttributes().get("workflow");
		workflow = dao.read(wf_id);
		checkEntity(workflow);
	}

	@Override
	public Date getModificationDate() {
		return workflow.getLastModified();
	}
	
	@Override
	public String representPlainText() {
		StringBuilder sb = new StringBuilder();
		sb.append("Workflow ").append(workflow.getId()).append('\n');
		sb.append("Created: ").append(workflow.getCreated()).append('\n');
		sb.append("Last-Modified: ").append(workflow.getLastModified()).append('\n');
		sb.append(workflow.getScufl());
		return sb.toString();
	}

	@Override
	public String representXML() {
		WorkflowDocument wfDoc = WorkflowDocument.Factory.newInstance();
		net.sf.taverna.service.xml.Workflow wfElem = wfDoc.addNewWorkflow();
		if (workflow.getOwner() != null) {
			wfElem.addNewOwner().setHref(uriFactory.getURI(workflow.getOwner()));
		}
		wfElem.setId(workflow.getId());
		try {
			wfElem.addNewScufl().set(XmlString.Factory.parse(workflow.getScufl()));
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			logger.warn("An error occured", e);
		}
		return wfElem.xmlText(xmlOptions);
	}

}
