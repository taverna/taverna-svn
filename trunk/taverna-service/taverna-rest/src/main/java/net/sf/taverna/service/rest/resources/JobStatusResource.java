package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.rest.resources.representation.AbstractText;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class JobStatusResource extends AbstractJobResource {
	private static Logger logger = Logger.getLogger(JobStatusResource.class);

	public JobStatusResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new Text());
	}

	class Text extends AbstractText {
		@Override
		public String getText() {
			return job.getStatus().name();
		}
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public long maxSize() {
		return 100;
	}

	@Override
	public void put(Representation entity) {
		if (! isEntityValid(entity, MediaType.TEXT_PLAIN)) {
			return;
		}
		String status;
		try {
			status = entity.getText();
		} catch (IOException e) {
			logger.warn("Could not read job status", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read job status");
			return;
		}
		try {
			job.setStatus(Job.Status.valueOf(status));
		} catch (IllegalArgumentException e) {
			logger.warn("Attempt to set invalid job status: " + status, e);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Unknown job status: " + status);
			return;
		}
		daoFactory.commit();
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		logger.info("Updated status " + job);
	}
}
