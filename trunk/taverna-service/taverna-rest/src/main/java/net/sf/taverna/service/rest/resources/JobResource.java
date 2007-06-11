package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.StatusType;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class JobResource extends AbstractJobResource {

	private static Logger logger = Logger.getLogger(JobResource.class);

	public JobResource(Context context, Request request, Response response) {
		super(context, request, response);
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
			if (job.getOwner() != null) {
				jobElement.addNewOwner().setHref(
					uriFactory.getURI(job.getOwner()));
				jobElement.getOwner().setUsername(job.getOwner().getUsername());
			}
			if (job.getInputs() != null) {
				jobElement.addNewInputs().setHref(
					uriFactory.getURI(job.getInputs()));
			}
			if (job.getOutputs() != null) {
				jobElement.addNewOutputs().setHref(
					uriFactory.getURI(job.getOutputs()));
			}
			if (job.getProgressReport() != null) {
				jobElement.addNewReport().setHref(uriFactory.getURIReport(job));
			}
			return jobDoc;
		}
	}

}
