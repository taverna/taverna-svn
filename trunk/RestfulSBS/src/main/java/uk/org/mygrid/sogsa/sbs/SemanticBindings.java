package uk.org.mygrid.sogsa.sbs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class SemanticBindings extends BindingsList {

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

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public boolean allowGet() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Create a new binding
	 */
	@Override
	public void post(Representation entity) {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "posting a binding");
		Form form = new Form(entity);
		String rdf = form.getFirstValue("rdf");

		// create a binding with RDF inside
		UUID randomUUID = UUID.randomUUID();
		// use database layer to store the rdf supplied in the request
		addBinding(randomUUID.toString(), rdf);
		Representation rep = new StringRepresentation("Binding succesfully created " + getRequest().getResourceRef().getIdentifier() + "/"
				+ randomUUID.toString(),
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
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "getting a binding");
		System.out.println("getting a binding");
		String bindingKey = (String) getRequest().getAttributes().get("key");
		// work some magic on anzo and pull the rdf and bits back
		return super.getRepresentation(variant);
	}

}
