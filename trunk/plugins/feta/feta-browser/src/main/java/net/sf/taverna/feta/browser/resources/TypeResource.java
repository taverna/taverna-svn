package net.sf.taverna.feta.browser.resources;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;



public class TypeResource extends AbstractResource {

	private String typeName;
	private QName qname;
	private org.openrdf.concepts.rdfs.Class type;

	public TypeResource(Context context, Request request, Response response) {
		super(context, request, response);
		typeName = (String) request.getAttributes().get("name");
		qname = new QName("http://www.mygrid.org.uk/ontology#", typeName);
		type = serviceRegistry.getClass(qname);
		if (type != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}	
	}
	
	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
		model.put("typeName", typeName);
		List<ServiceDescription> consumingServices = serviceRegistry.getServicesConsuming(type);
		model.put("consumingServices", consumingServices);
		List<ServiceDescription> producingServices = serviceRegistry.getServicesProducing(type);
		model.put("producingServices", producingServices);
		
		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"type.vm", model, MediaType.TEXT_HTML);
		return templateRepr;
	}

}
