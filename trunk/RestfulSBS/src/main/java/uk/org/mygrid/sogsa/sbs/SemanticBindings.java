package uk.org.mygrid.sogsa.sbs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class SemanticBindings extends BindingsList {
	/** Store binding keys locally for a quick lookup */
	private static List<String> bindings = new ArrayList<String>();

	public SemanticBindings(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		super.delete();
	}

	/**
	 * Create a new binding
	 */
	@Override
	public void post(Representation entity) {
		// create a binding with no RDF inside
		UUID randomUUID = UUID.randomUUID();
		getBindingList().put(randomUUID.toString(),
				new Binding(randomUUID.toString()));
		// use database layer to store the rdf supplied in the request
		Representation rep = new StringRepresentation("Item created",
				MediaType.TEXT_PLAIN);
		// Indicates where the new resource is located.
		rep.setIdentifier(getRequest().getResourceRef().getIdentifier() + "/"
				+ randomUUID.toString());
		getResponse().setEntity(rep);
	}

	@Override
	public void put(Representation entity) {
		// TODO Auto-generated method stub
		super.put(entity);
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		String bindingKey = (String) getRequest().getAttributes().get("key");
		// work some magic on anzo and pull the rdf and bits back
		return super.getRepresentation(variant);
	}

}
