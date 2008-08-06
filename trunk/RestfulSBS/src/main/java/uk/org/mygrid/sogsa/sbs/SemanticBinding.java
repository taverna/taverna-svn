package uk.org.mygrid.sogsa.sbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SemanticBinding extends BindingsList {

	private SemanticBindingInstance binding;

	private String key;

	public SemanticBinding(Context context, Request request, Response response) {
		super(context, request, response);
		for (Entry set : getRequest().getAttributes().entrySet()) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, set.getKey() + " " + set.getValue());
		}
		this.key = (String) getRequest().getAttributes().get("binding");

		// Check if this binding actually exists
		SemanticBindingInstance binding = null;
		try {
			if ((binding = getBinding(key)) != null) {
//				this.binding = getBinding(key);
				this.binding = binding;
				getVariants().add(new Variant(MediaType.TEXT_XML));
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, "problem with the request");
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
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "getting a binding");
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
				eltDescription.appendChild(d.createTextNode(this.binding
						.getRdf()));
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

	/**
	 * Replace all the RDF in a binding with a new set
	 */
	@Override
	public void put(Representation entity) {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "you wanted to add something to:" + this.key);
		Form form = new Form(entity);
		String rdf = form.getFirstValue("rdf");

		if (rdf == null) {
			String url = form.getFirstValue("url");
			rdf = loadRDF(url);
		}
		//TODO needs to handle an exception if the rdf is still null
		try {
			updateRDF(this.key, rdf);
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, "Could not update RDF for " + this.key + " " + e);
		}
	}

	@Override
	public void delete() {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "you wanted to delete me");
		removeBinding(this.key);
	}
	
	/**
	 * Load the rdf from the supplied URL
	 * 
	 * @param url
	 * @return
	 */
	private String loadRDF(String url) {
		String rdfString = new String();
		URL rdfURL = null;
		try {
			rdfURL = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(rdfURL.openStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String inputLine;

		try {
			while ((inputLine = in.readLine()) != null)
				rdfString = rdfString + inputLine;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return rdfString;
	}

}
