package net.sf.taverna.feta.browser.resources;

import java.util.Map;

import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class IndexResource extends AbstractResource {

	public IndexResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		model.put("lastUpdated", serviceRegistry.getLastUpdated());
		
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"index.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}
}
