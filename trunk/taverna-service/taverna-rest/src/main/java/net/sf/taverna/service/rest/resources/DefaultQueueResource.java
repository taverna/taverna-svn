package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class DefaultQueueResource extends AbstractResource {

	private Queue queue;
	
	public DefaultQueueResource(Context context, Request request, Response response) {
		super(context,request,response);
		queue=DAOFactory.getFactory().getQueueDAO().defaultQueue();
		addRepresentation(new QueueVelocityRepresentation());
	}
	
	
	class QueueVelocityRepresentation extends VelocityRepresentation {
		public QueueVelocityRepresentation() {
			super("queue.vm");
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Map<String,Object> model = new HashMap<String, Object>();
			model.put("queue",queue);
			model.put("jobs",queue.getJobs());
			model.put("uriFactory", URIFactory.getInstance(getRequest()));
			model.put("currentuser", getAuthUser());
			return model;
		}
	}
}
