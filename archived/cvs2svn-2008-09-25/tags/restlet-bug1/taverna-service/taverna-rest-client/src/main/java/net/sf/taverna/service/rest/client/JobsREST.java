package net.sf.taverna.service.rest.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.Jobs;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class JobsREST extends LinkedREST<Jobs> {

	private static Logger logger = Logger.getLogger(JobsREST.class);

	public JobsREST(RESTContext context, Jobs jobs) {
		super(context, jobs);
	}

	public JobsREST(RESTContext context, Reference uri) {
		super(context, uri, Jobs.class);
	}

	public JobsREST(RESTContext context, Reference uri, Jobs document) {
		super(context, uri, document);
	}

	public List<JobREST> getJobs() {
		List<JobREST> jobs = new ArrayList<JobREST>();
		for (Job job : getDocument().getJobArray()) {
			jobs.add(new JobREST(context, job));
		}
		return jobs;
	}

	public Iterator<JobREST> iterator() {
		// FIXME: Should not create all the JobREST objects before needed
		return getJobs().iterator();
	}


	public JobREST add(WorkflowREST wf) throws NotSuccessException {
		// TODO: Support DataREST as inputdoc
		JobDocument jobDoc = JobDocument.Factory.newInstance();
		jobDoc.addNewJob().addNewWorkflow().setHref(wf.getURI());
		
		Response response = context.post(getURIReference(), jobDoc);
		if (response.getRedirectRef() == null) {
			logger.error("Did not get redirect reference for job for wf " + wf);
			return null;
		}
		return new JobREST(context, response.getRedirectRef());
	}

}
