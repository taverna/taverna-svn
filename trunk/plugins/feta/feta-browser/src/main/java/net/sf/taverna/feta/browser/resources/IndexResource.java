package net.sf.taverna.feta.browser.resources;

import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

public class IndexResource extends AbstractResource {

	public IndexResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public String getPageTemplate() {
		return "index.vm";
	}

	@Override
	public String getPageTitle() {
		return "Feta-annotated services";
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		return model;
	}
}
