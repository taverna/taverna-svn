package net.sf.taverna.service.rest.client;

import java.io.IOException;

import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.StatusType;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class JobREST extends OwnedREST<Job> {
	
	private static Logger logger = Logger.getLogger(JobREST.class);
	
	public JobREST(RESTContext context, Job job) {
		super(context, job);
	}

	public JobREST(RESTContext context, Reference uri) {
		super(context, uri, Job.class);
	}

	public JobREST(RESTContext context, Reference uri, Job job) {
		super(context, uri, job);
	}

	public WorkflowREST getWorkflow() {
		return new WorkflowREST(context, getDocument().getWorkflow());
	}
	
	private Reference getStatusURI() {
		String statusURI = getDocument().getStatus().getHref();
		if (statusURI == null) { 
			return null;
		}
		return new Reference(getURIReference(), statusURI);
	}
	
	public StatusType.Enum getStatus() throws RESTException {
		Reference statusURI = getStatusURI();
		if (statusURI == null) {
			// no link for live-update, return what we have
			return (StatusType.Enum) getDocument().getStatus().enumValue();
		}
		// Return the freshest of the freshest status with a new get()
		Response response = context.get(statusURI, MediaType.TEXT_PLAIN);
		String status;
		try {
			status = response.getEntity().getText();
		} catch (IOException e) {
			throw new RESTException("Could not receive status", e);
		}
		logger.debug("Status for " + this + ": " + status);
		return StatusType.Enum.forString(status);
	}

	public void setStatus(StatusType.Enum queued) throws NotSuccessException {
		Reference statusURI = getStatusURI();
		if (statusURI != null) {
			Response response =
				context.put(statusURI, queued.toString(), MediaType.TEXT_PLAIN);
		} else {
			Job job = Job.Factory.newInstance();
			job.addNewStatus().set(queued);
			
		}
		
		
	}

}
