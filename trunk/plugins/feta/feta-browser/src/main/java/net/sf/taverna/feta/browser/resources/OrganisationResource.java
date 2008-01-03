package net.sf.taverna.feta.browser.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.ServiceComparator;
import net.sf.taverna.feta.browser.util.URLFactory;
import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class OrganisationResource extends Resource {

	private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
	private URLFactory urlFactory = URLFactory.getInstance();
	private Organisation organisation;
	private String organisationName;

	public OrganisationResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		String name = (String) request.getAttributes().get("name");
		organisationName = Reference.decode(name);
		organisation = serviceRegistry.getOrganisationByName(organisationName);
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("urlFactory", urlFactory);
		model.put("organisation", organisation);

		List<ServiceDescription> services = serviceRegistry
				.getServicesProvidedByOrganisation(organisationName);
		Collections.sort(services, ServiceComparator.getInstance());
		model.put("services", services);

		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"organisation.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}
}
