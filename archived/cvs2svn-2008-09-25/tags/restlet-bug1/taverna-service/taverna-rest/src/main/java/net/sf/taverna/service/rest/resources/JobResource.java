package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.util.Date;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.StatusType;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class JobResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(JobResource.class);

	private Job job;

	public JobResource(Context context, Request request, Response response) {
		super(context, request, response);
		JobDAO dao = daoFactory.getJobDAO();
		String job_id = (String) request.getAttributes().get("job");
		job = dao.read(job_id);
		checkEntity(job);
		addRepresentation(new Text());
		addRepresentation(new XML());
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
			if (job.getOutputDoc() != null) {
				sb.append("Outputs: ").append(
					uriFactory.getURI(job.getOutputDoc())).append('\n');
			}
			if (job.getProgressReport() != null) {
				sb.append("Progress: ").append(uriFactory.getURI(job)).append(
					"progress").append('\n');
			}
			return sb.toString();
		}
	}

	class XML extends AbstractREST {
		@Override
		public JobDocument getXML() {
			JobDocument jobDoc = JobDocument.Factory.newInstance(xmlOptions);
			net.sf.taverna.service.xml.Job jobElement = jobDoc.addNewJob();

			jobElement.addNewStatus().set(
				StatusType.Enum.forString(job.getStatus().name()));
			jobElement.getStatus().setHref(uriFactory.getURIStatus(job));

			jobElement.addNewWorkflow().setHref(
				uriFactory.getURI(job.getWorkflow()));

			if (job.getInputs() != null) {
				jobElement.addNewInputs().setHref(
					uriFactory.getURI(job.getInputs()));
			}
			if (job.getOutputDoc() != null) {
				jobElement.addNewOutputs().setHref(
					uriFactory.getURI(job.getOutputDoc()));
			}
			if (job.getProgressReport() != null) {
				jobElement.addNewReport().setHref(uriFactory.getURIReport(job));
			}
			return jobDoc;
		}
	}

	@Override
	public Date getModificationDate() {
		return job.getLastModified();
	}

}
