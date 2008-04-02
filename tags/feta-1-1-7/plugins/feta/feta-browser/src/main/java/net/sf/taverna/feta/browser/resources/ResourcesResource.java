package net.sf.taverna.feta.browser.resources;

import java.util.List;
import java.util.Map;

import org.openrdf.concepts.rdfs.Class;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

public class ResourcesResource extends AbstractResource {

	public ResourcesResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public String getPageTemplate() {
		return "resources.vm";
	}

	@Override
	public String getPageTitle() {
		return "All resources";
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		List<Class> resources = serviceRegistry.getResourceClasses();
		model.put("resources", utils.extractBioNames(resources));
		return model;
	}

}
