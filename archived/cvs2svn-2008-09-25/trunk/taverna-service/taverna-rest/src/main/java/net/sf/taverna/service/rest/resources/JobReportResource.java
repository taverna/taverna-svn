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

public class JobReportResource extends AbstractJobResource {
	
	private static Logger logger = Logger.getLogger(JobReportResource.class);

	public JobReportResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new Text());
		// TODO Some fancy velocity representation
	}
	
	class Text extends AbstractText {
		
		@Override
		public String getText() {
			return job.getProgressReport();
		}
		
		@Override
		public MediaType getMediaType() {
			return reportType;
		}
	}
	
	@Override
	public boolean allowPut() {
		return true;
	}
	
	@Override
	public long maxSize() {
		return Job.MAX_REPORT_SIZE;
	}
	
	@Override
	public void put(Representation entity) {
		logger.debug("About to set progress report for " + job);
		if (! isEntityValid(entity, reportType)) {
			return;
		}
		String report;
		try {
			report = entity.getText();
		} catch (IOException e) {
			logger.warn("Could not read job status", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read job status");
			return;
		}
		logger.debug("Received:\n" +report);
		try {
			job.setProgressReport(report);
		} catch (IllegalArgumentException e) {
			logger.warn("Attempt to set invalid job progress report: " + report, e);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Invalid progress report: " + report);
			return;
		}
		daoFactory.commit();
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		logger.info("Updated report " + job);
	}
	

}
