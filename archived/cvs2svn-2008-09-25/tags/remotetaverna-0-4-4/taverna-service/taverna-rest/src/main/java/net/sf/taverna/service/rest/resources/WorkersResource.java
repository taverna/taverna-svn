package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.WorkerInitialisation;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class WorkersResource extends AbstractResource {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WorkersResource.class);
	
	public WorkersResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new WorkersVelocityRepresentation());
	}
	
	public boolean allowPost() {
		return true;
	}

	public void handlePost() {
		try {
			if (getAuthUser().isAdmin() && daoFactory.getWorkerDAO().all().size()<4) {
				
				WorkerInitialisation.createNew();
				getResponse().setRedirectRef(getRequest().getReferrerRef());
				getResponse().setStatus(Status.REDIRECTION_FOUND);
			}
			else {
				getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			}
		}
		catch(Throwable e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred creating the worker: "+e.getCause().getMessage());
		}
	}

	class WorkersVelocityRepresentation extends VelocityRepresentation {
		
		@Override
		protected Map<String, Object> getDataModel() {
			List<Worker> workers = DAOFactory.getFactory().getWorkerDAO().all();
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("uriFactory", uriFactory);
			result.put("workers", workers);
			boolean isAdmin = getAuthUser().isAdmin();
			result.put("isAdmin", isAdmin);
			return result;
		}

		@Override
		protected String pageTitle() {
			return "Workers";
		}

		@Override
		protected String templateName() {
			return "workers.vm";
		}
		
		
	}
}
