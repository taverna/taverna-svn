package net.sf.taverna.service.rest.resources;

import java.util.List;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class WorkerResource extends AbstractResource {
	
	private static Logger logger = Logger.getLogger(WorkerResource.class);

	private Worker worker;
	
	public WorkerResource(Context context, Request request, Response response) {
		super(context, request, response);
		String worker_id = (String)request.getAttributes().get("worker");
		worker=DAOFactory.getFactory().getWorkerDAO().read(worker_id);
		checkEntity(worker);
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void delete() {
		if (getAuthUser().isAdmin()) {
			if (worker != null) {
				if (!worker.isBusy()) {
					deleteWorker();
				} else {
					getResponse().setStatus(
						Status.CLIENT_ERROR_PRECONDITION_FAILED);
				}
			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
	}

	private void deleteWorker() {
		WorkerDAO workerDAO = DAOFactory.getFactory().getWorkerDAO();
		JobDAO jobDAO=DAOFactory.getFactory().getJobDAO();
		String worker_id=worker.getId();
		
		//first remove the worker jobs to avoid a foreign key constraint.
		List<Job> jobs=worker.unassignJobs();
		for (Job job : jobs) jobDAO.update(job);
		workerDAO.update(worker);
		
		//now we can delete it. But only if there are no remaining jobs (which could be running).
		if (worker.getWorkerJobs().isEmpty()) {
			workerDAO.delete(worker);
			worker=null;
			DAOFactory.getFactory().commit();
			logger.info("Deleted worker:"+worker_id);
			getResponse().setStatus(Status.REDIRECTION_FOUND);
			getResponse().setRedirectRef(getRequest().getReferrerRef());
		}
		else {
			getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "The worker currently has active jobs");
		}
	}
	
}
