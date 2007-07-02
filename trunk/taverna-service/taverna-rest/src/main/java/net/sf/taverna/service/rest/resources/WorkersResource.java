package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class WorkersResource extends AbstractResource{

	public WorkersResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new WorkersVelocityRepresentation());
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
			result.put("monkey", "bob monkhouse");
			return result;
		}
	}
	
}
