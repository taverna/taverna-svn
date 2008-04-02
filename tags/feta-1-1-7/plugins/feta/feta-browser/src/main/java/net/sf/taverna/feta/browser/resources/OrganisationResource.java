package net.sf.taverna.feta.browser.resources;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.util.ServiceComparator;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class OrganisationResource extends AbstractResource {

	private Organisation organisation;
	private String organisationName;

	public OrganisationResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		String name = (String) request.getAttributes().get("name");
		organisationName = Reference.decode(name);
		organisation = serviceRegistry.getOrganisationByName(organisationName);
		if (organisation != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public String getPageTemplate() {
		return "organisation.vm";
	}

	@Override
	public String getPageTitle() {
		return "Organisation " + organisationName;
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		model.put("organisation", organisation);

		List<ServiceDescription> services = serviceRegistry
				.getServicesProvidedByOrganisation(organisationName);
		Collections.sort(services, ServiceComparator.getInstance());
		model.put("services", services);
		return model;
	}
}
