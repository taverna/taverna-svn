package net.sf.taverna.feta.browser.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.URIFactory;
import net.sf.taverna.feta.browser.util.Utils;
import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.apache.commons.collections.SetUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

public abstract class AbstractResource extends Resource {

	protected ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
	protected URIFactory uriFactory;
	protected Utils utils = Utils.getInstance();

	public AbstractResource(Context context, Request request, Response response) {
		super(context, request, response);
		uriFactory = URIFactory.getInstance(request);
	}

	public abstract String getPageTemplate();

	public abstract String getPageTitle();

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		return makeRepresentation(model);
	}

	public VelocityRepresentation makeRepresentation(Map<String, Object> model) {
		
		return new VelocityRepresentation("layout.vm", model,
				MediaType.TEXT_HTML);
	}

	protected Map<String, Object> makeModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("uriFactory", uriFactory);
		model.put("utils", utils);
		model.put("lastUpdated", serviceRegistry.getLastUpdated());
		model.put("pageTemplate", getPageTemplate());
		model.put("pageTitle", getPageTitle());
		return model;
	}

}