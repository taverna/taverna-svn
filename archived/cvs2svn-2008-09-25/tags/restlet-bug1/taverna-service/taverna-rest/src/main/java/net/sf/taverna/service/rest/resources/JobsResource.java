package net.sf.taverna.service.rest.resources;

import java.io.IOException;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.Jobs;
import net.sf.taverna.service.xml.JobsDocument;
import net.sf.taverna.service.xml.Workflow;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class JobsResource extends AbstractUserResource {

	private static Logger logger = Logger.getLogger(JobsResource.class);

	public JobsResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new URIList());
		addRepresentation(new XML());
	}

	class URIList extends AbstractURIList<Job> {
		@Override
		public Iterator<Job> iterator() {
			return user.getJobs().iterator();
		}
	}

	class XML extends AbstractREST {
		@Override
		public XmlObject getXML() {
			JobsDocument doc = JobsDocument.Factory.newInstance();
			Jobs jobs = doc.addNewJobs();
			for (Job job : user.getJobs()) {
				jobs.addNewJob().setHref(uriFactory.getURI(job));
			}
			return doc;
		}
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void post(Representation entity) {
		if (!restType.includes(entity.getMediaType())) {
			System.out.println("Wrong type: " + entity.getMediaType());
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + restType);
			return;
		}
		JobDocument jobDoc;
		try {
			jobDoc = JobDocument.Factory.parse(entity.getStream());
		} catch (XmlException ex) {
			logger.warn("Could not parse job document", ex);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Could not parse as XML");
			return;
		} catch (IOException ex) {
			logger.warn("Could not read XML", ex);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			return;
		}

		Job job = new Job();
		// Force owner by URI, ignore 'owner' in XML
		job.setOwner(user);
		user.getJobs().add(job);

		Workflow wf = jobDoc.getJob().getWorkflow();
		if (wf == null) {
			logger.error("No workflow provided");
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Workflow is required");
			return;
		}
		if (wf.getHref() == null) {
			// FIXME: create the new Workflow object instead
			logger.error("Non-xlink workflow in job submission");
			getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
				"Workflow must be given as xlink");
			return;
		}
		// TODO: Support external (public) workflows
		job.setWorkflow(uriToDAO.getResource(wf.getHref(),
			net.sf.taverna.service.datastore.bean.Workflow.class));

		Data inputs = jobDoc.getJob().getInputs();
		if (inputs != null) {
			if (inputs.getHref() == null) {
				// FIXME: create the new DataDoc object instead
				logger.error("Only supports job workflows by xlink references");
				getResponse().setStatus(
					Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
					"Input data must be given as xlink");
				return;
			}
			// TODO: Support external (public) DataDocs
			job.setInputs(uriToDAO.getResource(inputs.getHref(), DataDoc.class));
		}
		daoFactory.getJobDAO().create(job);
		daoFactory.commit();
		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setRedirectRef(uriFactory.getURI(job));
		logger.info("Created new " + job);
	}

}
