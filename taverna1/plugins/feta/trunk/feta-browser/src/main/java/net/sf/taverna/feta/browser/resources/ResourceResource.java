package net.sf.taverna.feta.browser.resources;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ResourceResource extends AbstractResource {

	protected org.openrdf.concepts.rdfs.Class resource;
	protected String resourceName;
	protected QName qname;

	public ResourceResource(Context context, Request request, Response response) {
		super(context, request, response);
		resourceName = (String) request.getAttributes().get("name");
		qname = new QName("http://www.mygrid.org.uk/ontology#", resourceName);
		resource = serviceRegistry.getClass(qname);
		if (resource != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public String getPageTemplate() {
		return "resource.vm";
	}

	@Override
	public String getPageTitle() {
		return "Resource " + resourceName;
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		List<ServiceDescription> usesResources = serviceRegistry
				.getServicesUsingResource(resource);
		model.put("resourceName", resourceName);
		model.put("services", usesResources);
		return model;
	}

}
