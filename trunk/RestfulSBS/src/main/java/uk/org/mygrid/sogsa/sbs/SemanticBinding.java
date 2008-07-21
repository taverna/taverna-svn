package uk.org.mygrid.sogsa.sbs;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SemanticBinding extends BindingsList {

	private Binding binding;

	private String key;

	public SemanticBinding(Context context, Request request, Response response) {
		super(context, request, response);
		for (Entry set: getRequest().getAttributes().entrySet()){
			java.util.logging.Logger.getLogger("org.mortbay.log").log(Level.WARNING, set.getKey() +" " + set.getValue());
		}
		this.key = (String) getRequest().getAttributes().get("binding");

		// Check if this binding actually exists
		

		if (hasBinding(key)) {
			this.binding = getBinding(key);
			// Define the supported variant.
			getVariants().add(new Variant(MediaType.TEXT_XML));
		}

	}

	@Override
	public boolean allowDelete() {
		// TODO Auto-generated method stub
		return super.allowDelete();
	}

	@Override
	public boolean allowGet() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Allow new RDF to be added to the {@link Binding}
	 */
	@Override
	public boolean allowPut() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Return an XML document with the RDF for the binding inside
	 */
	@Override
	public Representation getRepresentation(Variant variant) {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(Level.WARNING, "getting a binding");
		if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
			// Generate the XML representation of this resource.
			try {
				// Generate a DOM document representing the item.
				DomRepresentation representation = new DomRepresentation(
						MediaType.TEXT_XML);
				Document d = representation.getDocument();

				Element eltItem = d.createElement("binding");
				d.appendChild(eltItem);
				Element eltName = d.createElement("key");
				eltName.appendChild(d.createTextNode(this.binding.getKey()));
				eltItem.appendChild(eltName);

				Element eltDescription = d.createElement("description");
				eltDescription.appendChild(d.createTextNode(this.binding.getRdf()));
				eltItem.appendChild(eltDescription);

				d.normalizeDocument();

				// Returns the XML representation of this document.
				return representation;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;

	}

	@Override
	public void put(Representation entity) {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(Level.WARNING, "you wanted to add something to me");
		// get the request, check the database to see if there is some RDF
		// with the binding key supplied. If so then add it

	}

	@Override
	public void delete() {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(Level.WARNING, "you wanted to delete me");
//		if (binding != null) {
//			// remove from database layer and then from bindingList
//			getBindingList().remove(binding);
//		}
	}

	@Override
	public void post(Representation arg0) {
		// TODO Auto-generated method stub
		// add new binding to database layer
	}

}
