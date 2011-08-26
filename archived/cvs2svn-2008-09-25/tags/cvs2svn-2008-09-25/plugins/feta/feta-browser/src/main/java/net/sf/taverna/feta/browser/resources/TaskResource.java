package net.sf.taverna.feta.browser.resources;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openrdf.concepts.rdfs.Class;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class TaskResource extends AbstractResource {

	protected QName qname;
	protected Class task;
	protected String taskName;

	public TaskResource(Context context, Request request, Response response) {
		super(context, request, response);
		taskName = (String) request.getAttributes().get("name");
		qname = new QName("http://www.mygrid.org.uk/ontology#", taskName);
		task = serviceRegistry.getClass(qname);
		if (task != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public String getPageTemplate() {
		return "task.vm";
	}

	@Override
	public String getPageTitle() {
		return "Task " + taskName;
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		List<ServiceDescription> performing = serviceRegistry
				.getServicesPerforming(task);
		model.put("taskName", taskName);
		model.put("services", performing);
		return model;
	}

}
