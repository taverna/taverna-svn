package net.sf.taverna.service.rest.resources;

import java.util.Date;

import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.xml.WorkflowDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class WorkflowResource extends AbstractOwnedResource<Workflow> {

	private static Logger logger = Logger.getLogger(WorkflowResource.class);

	private Workflow workflow;		

	public WorkflowResource(Context context, Request request, Response response) {
		super(context, request, response);
		String wf_id = (String) request.getAttributes().get("workflow");
		workflow = daoFactory.getWorkflowDAO().read(wf_id);
		checkEntity(workflow);
		setResource(workflow);
		addRepresentation(new Text());
		addRepresentation(new XML());
		addRepresentation(new Scufl());
	
	}

	class Text extends AbstractText {

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append("Workflow ").append(workflow.getId()).append('\n');
			sb.append("Created: ").append(workflow.getCreated()).append('\n');
			sb.append("Owner: ").append(uriFactory.getURIUser(workflow.getOwner())).append(
				'\n');
			sb.append(workflow.getScufl());
			return sb.toString();
		}
	}

	class XML extends AbstractOwnedXML<net.sf.taverna.service.xml.Workflow> {

		@Override
		public void addElements(net.sf.taverna.service.xml.Workflow wfElem) {
			super.addElements(wfElem);
			try {
				XmlString scufl = XmlString.Factory.parse(workflow.getScufl());
				wfElem.addNewScufl().set(scufl);
			} catch (XmlException e) {
				logger.warn("Could not include invalid XScufl for " + workflow,
					e);
			}
		}

		@Override
		public XmlObject createDocument() {
			WorkflowDocument doc = WorkflowDocument.Factory.newInstance();
			element = doc.addNewWorkflow();
			return doc;
		}
	}

	class Scufl extends AbstractText {
		@Override
		public String getText() {
			return workflow.getScufl();
		}

		@Override
		public MediaType getMediaType() {
			return scuflType;
		}
	}

	@Override
	public Date getModificationDate() {
		return workflow.getLastModified();
	}

}
