package net.sf.taverna.feta.browser.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.util.ServiceComparator;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServicesResource extends AbstractResource {

	public ServicesResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public String getPageTemplate() {
		return "services.vm";
	}

	@Override
	public String getPageTitle() {
		return "All services";
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		List<ServiceDescription> services = new ArrayList<ServiceDescription>();
		for (ServiceDescription service : serviceRegistry
				.getServiceDescriptions()) {
			services.add(service);
		}
		Collections.sort(services, ServiceComparator.getInstance());

		model.put("services", services);
		return model;
	}

}
