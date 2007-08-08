package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

public class DefaultQueueResource extends Resource {

	private Queue queue;

	private URIFactory uriFactory;

	public DefaultQueueResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		queue = DAOFactory.getFactory().getQueueDAO().defaultQueue();
		uriFactory = URIFactory.getInstance();
	}

	@Override
	public void handleGet() {
		getResponse().redirectTemporary(uriFactory.getURI(queue));
	}

}
