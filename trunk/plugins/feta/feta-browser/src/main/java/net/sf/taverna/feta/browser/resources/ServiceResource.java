package net.sf.taverna.feta.browser.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.URLFactory;
import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceResource extends Resource {

	private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
	private ServiceDescription service;

	private URLFactory urlFactory = URLFactory.getInstance();

	public ServiceResource(Context context, Request request, Response response) {
		super(context, request, response);
		String id = (String) request.getAttributes().get("id");
		service = serviceRegistry.getServiceDescription(id);
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("urlFactory", urlFactory);
		model.put("service", service);
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"service.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}

}
