package net.sf.taverna.feta.browser.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.ServiceComparator;
import net.sf.taverna.feta.browser.util.URIFactory;
import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServicesResource extends AbstractResource {

	public ServicesResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		List<ServiceDescription> services = new ArrayList<ServiceDescription>();
		for (ServiceDescription service : serviceRegistry
				.getServiceDescriptions()) {
			services.add(service);
		}
		Collections.sort(services, ServiceComparator.getInstance());

		model.put("services", services);
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"services.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}

}
