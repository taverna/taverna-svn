package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.DateUtils.humanDuration;
import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.Report;
import net.sf.taverna.service.xml.StatusType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.w3c.dom.Node;

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
		if (job == null) {
			// already non-existing, everything OK
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		}
		DAOFactory daoFactory = DAOFactory.getFactory();
		if (job.getQueue() != null) {
			QueueEntry entry = job.getQueue().removeJob(job);
			daoFactory.getQueueEntryDAO().delete(entry);
		}
		if (job.getOwner() != null) {
			User owner = job.getOwner();
			owner.getJobs().remove(job);
			daoFactory.getUserDAO().update(owner);
		}
		daoFactory.getJobDAO().delete(job);
		daoFactory.commit();
		logger.info("Deleted " + job);
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}

	@Override
	public void put(Representation entity) {
		if (!restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + restType);
			return;
		}
		JobDocument jobDoc;
                String text="";
		try {
                        text=entity.getText();
			jobDoc = JobDocument.Factory.parse(text);
		} catch (XmlException ex) {
			logger.warn("Could not parse job document\n"+text+"\n", ex);
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
		if (MediaType.APPLICATION_WWW_FORM.includes(entity.getMediaType())) {
			// only set redirection when coming from a browser.
			getResponse().setRedirectRef(getRequest().getReferrerRef());
			getResponse().setStatus(Status.REDIRECTION_FOUND);
		} else {
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		}
	}
	
	public void updateJob(JobDocument jobDoc) {
		net.sf.taverna.service.xml.Job jobElem = jobDoc.getJob();
		if (jobElem.getInputs() != null) {
			job.setInputs(uriToDAO.getResource(
				jobElem.getInputs().getHref(), DataDoc.class));
		}
		if (jobElem.getOutputs() != null) {
			job.setOutputs(uriToDAO.getResource(
				jobElem.getOutputs().getHref(), DataDoc.class));
		}
		if (jobElem.getUpdateInterval() != null) {
			GDuration interval = jobElem.getUpdateInterval();
			job.setUpdateInterval(interval.toString());
		}
		if (jobElem.getTitle() != null) {
			job.setName(jobElem.getTitle());
		}
		if (jobElem.getReport() != null) {
			Report report = jobElem.getReport();
			Node firstChild = report.getDomNode().getFirstChild();
			if (firstChild == null) {
				job.setProgressReport(null);
			} else {
				// Don't save outer, so don't use xmlOptions
				job.setProgressReport(report.xmlText());
			}			
		}
		if (jobElem.getConsole() != null) {
			job.setConsole(jobElem.getConsole().getStringValue());
		}
		if (jobElem.getStatus() != null) {
			job.setStatus(Job.Status.valueOf(jobElem.getStatus().getStringValue()));
		}
		if (jobElem.getUpdateInterval() != null) {
			job.setUpdateInterval(jobElem.getUpdateInterval().toString());
		}
		daoFactory.getJobDAO().update(job);
		daoFactory.commit();
		logger.info("Updated " + job);
		logger.debug("job xml \n" + jobDoc);
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
			jobElement.addNewConsole().setHref(uriFactory.getURIConsole(job));
		}
	}
	
	class JobVelocityRepresentation extends VelocityRepresentation
	{
		public JobVelocityRepresentation() {
			
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Map<String,Object> model = new HashMap<String, Object>();
			model.put("job",job);
			
			model.put("workflow", job.getWorkflow());
			model.put("workflowUri",uriFactory.getURI(job.getWorkflow()));
		
			if (job.getInputs()!=null) {
				model.put("input", job.getInputs());
				model.put("inputUri", uriFactory.getURI(job.getInputs()));
			}
			
			String progressUri=uriFactory.getURIReport(job);
			model.put("progressUri", progressUri);
			if (job.isFinished()) {
				model.put("isFinished",true);
				if (job.getOutputs()!=null) {
					model.put("output", job.getOutputs());
					model.put("outputUri",uriFactory.getURI(job.getOutputs()));
				}
			}
			else {
				model.put("isFinished",false);
			}
			
			model.put("jobstatusUri", uriFactory.getURIStatus(job));
			
			if (job.getConsole() != null && job.getConsole().length() > 0) {
				model.put("consoleUri", uriFactory.getURIConsole(job));
			}
			
			model.put("updateInterval", humanDuration(job.getUpdateInterval()));
			model.put("currentuser",getAuthUser());
			return model;
		}

		@Override
		protected String pageTitle() {
			return "Job "+job.getId();
		}

		@Override
		protected String templateName() {
			return "job.vm";
		}
		
		
	}

}
