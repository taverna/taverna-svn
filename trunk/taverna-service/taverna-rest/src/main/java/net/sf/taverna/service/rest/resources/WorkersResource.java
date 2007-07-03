package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class WorkersResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(WorkersResource.class);
	
	public WorkersResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new WorkersVelocityRepresentation());
	}
	
	public boolean allowCreate() {
		return true;
	}

	public void handleCreate() {
		Worker worker = new Worker();
		worker.setPassword("Bob"); //FIXME: hard-coded password
		worker.setQueue(daoFactory.getQueueDAO().defaultQueue());
		DAOFactory.getFactory().getWorkerDAO().create(worker);
		DAOFactory.getFactory().commit();
	}

	class WorkersVelocityRepresentation extends VelocityRepresentation {
		public WorkersVelocityRepresentation() {
			super("workers.vm");
		}

		@Override
		protected Map<String, Object> getDataModel() {
			List<Worker> workers = DAOFactory.getFactory().getWorkerDAO().all();
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("workers", workers);
			return result;
		}
	}
}
