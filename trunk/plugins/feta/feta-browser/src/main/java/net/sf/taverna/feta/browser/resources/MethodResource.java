package net.sf.taverna.feta.browser.resources;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.openrdf.concepts.rdfs.Class;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class MethodResource extends AbstractResource {

	protected QName qname;
	protected Class method;
	protected String methodName;

	public MethodResource(Context context, Request request, Response response) {
		super(context, request, response);
		methodName = (String) request.getAttributes().get("name");
		qname = new QName("http://www.mygrid.org.uk/ontology#", methodName);
		method = serviceRegistry.getClass(qname);
		if (method != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		List<ServiceDescription> usingMethod = serviceRegistry
				.getServicesUsingMethod(method);
		model.put("methodName", methodName);
		model.put("services", usingMethod);

		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"method.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}

}
