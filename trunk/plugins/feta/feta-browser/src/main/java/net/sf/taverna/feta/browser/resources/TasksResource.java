package net.sf.taverna.feta.browser.resources;

import java.util.List;
import java.util.Map;

import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.openrdf.concepts.rdfs.Class;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class TasksResource extends AbstractResource {

	public TasksResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		List<Class> tasks = serviceRegistry.getTaskClasses();
		model.put("tasks", utils.extractBioNames(tasks));	
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"tasks.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}
	
	
	
}
