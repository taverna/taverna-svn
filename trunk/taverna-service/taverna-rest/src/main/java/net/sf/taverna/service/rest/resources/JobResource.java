package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.StatusType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class JobResource extends AbstractJobResource {

	private static Logger logger = Logger.getLogger(JobResource.class);

	public JobResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new JobVelocityRepresentation());
		addRepresentation(new Text());
		addRepresentation(new XML());
	}
	
	@Override
	public boolean allowPut() {
		return true;
	}
	
	
	
	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void delete() {
		if (job!=null) {
			try {
				QueueEntry entry = DAOFactory.getFactory().getQueueDAO().defaultQueue().removeJob(job);
				DAOFactory.getFactory().getQueueEntryDAO().delete(entry);
				DAOFactory.getFactory().getJobDAO().delete(job);
				DAOFactory.getFactory().commit();
				getResponse().setRedirectRef(getRequest().getReferrerRef());
				getResponse().setStatus(Status.REDIRECTION_FOUND);
			}
			catch(Exception e) {
				logger.error("An error occurred trying to delete the job:"+job.getId());
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to delete the job:"+e.getCause().getMessage());
			}
		}
		else {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	@Override
	public void put(Representation entity) {
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
		updateJob(jobDoc);
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}
	
	public void updateJob(JobDocument jobDoc) {
		if (jobDoc.getJob().getInputs() != null) {
			job.setInputs(uriToDAO.getResource(
				jobDoc.getJob().getInputs().getHref(), DataDoc.class));
		}
		if (jobDoc.getJob().getOutputs() != null) {
			job.setOutputs(uriToDAO.getResource(
				jobDoc.getJob().getOutputs().getHref(), DataDoc.class));
		}
		if (jobDoc.getJob().getUpdateInterval() != null) {
			GDuration interval = jobDoc.getJob().getUpdateInterval();
			job.setUpdateInterval(interval.toString());
		}
		daoFactory.getJobDAO().update(job);
		daoFactory.commit();
		logger.info("Updated " + job);
	}

	class Text extends AbstractText {
		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append("Job ").append(job.getId()).append('\n');
			sb.append("Created: ").append(job.getCreated()).append('\n');
			sb.append("Last-Modified: ").append(job.getLastModified()).append(
				'\n');
			sb.append("Status: ").append(job.getStatus()).append('\n');

			sb.append("Workflow: ").append(uriFactory.getURI(job.getWorkflow())).append(
				'\n');
			if (job.getInputs() != null) {
				sb.append("Inputs: ").append(uriFactory.getURI(job.getInputs())).append(
					'\n');
			}
			if (job.getUpdateInterval() != null) {
				sb.append("Update-Interval: ").append(job.getUpdateInterval()).append('\n');
			}
			if (job.getOutputs() != null) {
				sb.append("Outputs: ").append(
					uriFactory.getURI(job.getOutputs())).append('\n');
			}
			if (job.getProgressReport() != null) {
				sb.append("Progress: ").append(uriFactory.getURI(job)).append(
					"progress").append('\n');
			}
			return sb.toString();
		}
	}

	class XML extends AbstractOwnedXML<net.sf.taverna.service.xml.Job> {
		

		@Override
		public JobDocument createDocument() {
			JobDocument doc = JobDocument.Factory.newInstance(xmlOptions);
			element = doc.addNewJob();
			return doc;
		}
		
		@Override
		public void addElements(net.sf.taverna.service.xml.Job jobElement) {
			super.addElements(jobElement);
			jobElement.addNewReport().setHref(uriFactory.getURIReport(job));
			jobElement.addNewStatus().set(
				StatusType.Enum.forString(job.getStatus().name()));
			jobElement.getStatus().setHref(uriFactory.getURIStatus(job));

			jobElement.addNewWorkflow().setHref(
				uriFactory.getURI(job.getWorkflow()));
			if (job.getInputs() != null) {
				jobElement.addNewInputs().setHref(
					uriFactory.getURI(job.getInputs()));
			}
			if (job.getOutputs() != null) {
				jobElement.addNewOutputs().setHref(
					uriFactory.getURI(job.getOutputs()));
			}
			if (job.getUpdateInterval() != null) {
				jobElement.setUpdateInterval(new GDuration(job.getUpdateInterval()));
			}
		}
	}
	
	class JobVelocityRepresentation extends VelocityRepresentation
	{
		public JobVelocityRepresentation() {
			super("job.vm");
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Map<String,Object> model = new HashMap<String, Object>();
			model.put("job",job);
			model.put("currentuser",getAuthUser());
			return model;
		}
	}
}
