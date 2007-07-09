package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.rest.resources.UserResource.UserVelocity;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.xml.Workflows;
import net.sf.taverna.service.xml.WorkflowsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class WorkflowsResource extends AbstractUserResource {

	private static Logger logger = Logger.getLogger(WorkflowsResource.class);

	private WorkflowDAO wfDao = daoFactory.getWorkflowDAO();

	public WorkflowsResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new Velocity());
		addRepresentation(new URIList());
		addRepresentation(new XML());
	}

	class URIList extends AbstractURIList<Workflow> {
		@Override
		public Iterator<Workflow> iterator() {
			return user.getWorkflows().iterator();
		}
	}

	class XML extends AbstractREST<Workflows> {
		@Override
		public XmlObject createDocument() {
			WorkflowsDocument doc =
				WorkflowsDocument.Factory.newInstance(xmlOptions);
			element = doc.addNewWorkflows();
			return doc;
		}

		@Override
		public void addElements(Workflows workflows) {
			for (Workflow wf : user.getWorkflows()) {
				workflows.addNewWorkflow().setHref(uriFactory.getURI(wf));
			}
		}
	}
	
	class Velocity extends OwnedVelocity {
		public Velocity() {
			super(user.getWorkflows(), "Workflows");
		}
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
		if (!scuflType.includes(entity.getMediaType())) {
			logger.warn("Post of unknown workflow media type "
				+ entity.getMediaType());
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + scuflType);
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
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			daoFactory.rollback();
			return;
		}
		wf.setOwner(user);
		wfDao.create(wf);
		daoFactory.commit();
		logger.info("Created " + wf);
		getResponse().setRedirectRef(uriFactory.getURI(wf));
		getResponse().setStatus(Status.SUCCESS_CREATED);
	}

}
