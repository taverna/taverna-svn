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

public class NamespaceResource extends AbstractResource {

	private String nsName;
	private QName qname;
	private org.openrdf.concepts.rdfs.Class type;

	public NamespaceResource(Context context, Request request, Response response) {
		super(context, request, response);
		nsName = (String) request.getAttributes().get("name");
		qname = new QName("http://www.mygrid.org.uk/ontology#", nsName);
		type = serviceRegistry.getClass(qname);
		if (type != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public String getPageTemplate() {
		return "namespace.vm";
	}

	@Override
	public String getPageTitle() {
		return "Namespace " + nsName;
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		model.put("nsName", nsName);
		List<ServiceDescription> consumingServices = serviceRegistry
				.getServicesConsumingNamespace(type);
		model.put("consumingServices", consumingServices);
		List<ServiceDescription> producingServices = serviceRegistry
				.getServicesProducingNamespace(type);
		model.put("producingServices", producingServices);
		return model;
	}

}
