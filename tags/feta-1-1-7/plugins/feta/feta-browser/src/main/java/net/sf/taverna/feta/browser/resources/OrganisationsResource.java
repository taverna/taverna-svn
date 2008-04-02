package net.sf.taverna.feta.browser.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Organisation;

public class OrganisationsResource extends AbstractResource {

	public OrganisationsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	public List<String> getOrganisations() {
		List<String> organisations = new ArrayList<String>();
		for (Organisation org : serviceRegistry.getOrganisations()) {
			String orgName = utils.firstOf(org.getHasOrganisationNameTexts());
			if (orgName == null || organisations.contains(orgName)) {
				continue;
			}
			organisations.add(orgName);
		}
		Collections.sort(organisations);
		return organisations;
	}

	@Override
	public String getPageTemplate() {
		return "organisations.vm";
	}

	@Override
	public String getPageTitle() {
		return "All organisations";
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		List<String> organisations = getOrganisations();
		model.put("organisations", organisations);
		return model;
	}

}
