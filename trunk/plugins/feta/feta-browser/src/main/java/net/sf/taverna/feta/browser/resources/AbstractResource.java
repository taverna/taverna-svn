package net.sf.taverna.feta.browser.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.Utils;
import net.sf.taverna.feta.browser.util.URIFactory;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

public class AbstractResource extends Resource {

	protected ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
	protected URIFactory uriFactory = URIFactory.getInstance();
	protected Utils utils = Utils.getInstance();


	public AbstractResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	protected Map<String, Object> makeModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("uriFactory", uriFactory);
		model.put("setUtils", utils);
		return model;
	}

}