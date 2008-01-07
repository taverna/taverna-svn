package net.sf.taverna.feta.browser.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.util.Utils;
import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Organisation;

public class OrganisationsResource extends AbstractResource {
	
	public OrganisationsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		List<String> organisations = new ArrayList<String>();
		for (Organisation org : serviceRegistry.getOrganisations()) {
			String orgName = utils.firstOf(org.getHasOrganisationNameTexts());
			if (orgName == null || organisations.contains(orgName)) {
				continue;
			}
			organisations.add(orgName);
		}
		Collections.sort(organisations);
		Map<String, Object> model = makeModel();
		model.put("organisations", organisations);
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"organisations.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}

}
