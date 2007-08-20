package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.JobDAO;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class AbstractJobResource extends AbstractOwnedResource<Job> {

	Job job;

	public AbstractJobResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		JobDAO dao = daoFactory.getJobDAO();
		String job_id = (String) request.getAttributes().get("job");
		job = dao.read(job_id);
		checkEntity(job);
		setResource(job);
	}

}
