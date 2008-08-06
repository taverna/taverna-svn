package uk.org.mygrid.sogsa.sbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import org.openrdf.model.URI;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class SemanticBindings extends BindingsList {

	public SemanticBindings(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void post(Representation entity) {
		Form form = new Form(entity);
		String query = form.getFirstValue("query");
		String queryBinding;
		try {
			queryBinding = queryAllBindings(query);
			if (queryBinding != null) {
				getResponse().setStatus(Status.SUCCESS_OK);
				// TODO put query in the rep
				Representation rep = new StringRepresentation(queryBinding,
						MediaType.TEXT_PLAIN);
				getResponse().setEntity(rep);

			} else {
				getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
				// TODO put query in the rep
				Representation rep = new StringRepresentation(
						"No query results returned", MediaType.TEXT_PLAIN);
				getResponse().setEntity(rep);

			}
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			Representation rep = new StringRepresentation(
					"Problem with the query request: " + e,
					MediaType.TEXT_PLAIN);
			getResponse().setEntity(rep);
		}
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		super.delete();
	}

	@Override
	public boolean allowGet() {
		// TODO Auto-generated method stub
		return true;
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

	/**
	 * Create a new binding for entity with the id of entityKey and the rdf
	 * passed in
	 */
	@Override
	public void put(Representation entity) {
		Form form = new Form(entity);
		String entityKey = form.getFirstValue("entityKey");
		String rdf = form.getFirstValue("rdf");

		if (rdf == null) {
			String url = form.getFirstValue("url");
			rdf = loadRDF(url);
		}

		// create a binding with RDF inside
		// use database layer to store the rdf supplied in the request
		try {
			addBinding(entityKey, rdf);
			Representation rep = new StringRepresentation(
					"Binding succesfully created "
							+ getRequest().getResourceRef().getIdentifier()
							+ "/" + entityKey, MediaType.TEXT_PLAIN);
			// Indicates where the new resource is located.
			rep.setIdentifier(getRequest().getResourceRef().getIdentifier()
					+ "/" + entityKey);
			getResponse().setEntity(rep);
		} catch (Exception e) {
			Representation rep = new StringRepresentation(
					"Binding creation unsuccesful " + e.toString(),
					MediaType.TEXT_PLAIN);
			getResponse().setEntity(rep);
		}

	}

	/**
	 * Get all the bindings
	 */
	@Override
	public Representation getRepresentation(Variant variant) {

		Iterable<URI> allBindings = null;
		try {
			allBindings = getAllBindings();
		} catch (SemanticBindingException e) {
			Representation rep = new StringRepresentation(
					"Problem with the server", MediaType.TEXT_PLAIN);
			getResponse().setEntity(rep);
			return rep;
		} catch (SemanticBindingNotFoundException e) {
			Representation rep = new StringRepresentation(
					"No bindings were found", MediaType.TEXT_PLAIN);
			getResponse().setEntity(rep);
			return rep;
		}
		String bindings = "<bindings>";
		for (URI binding : allBindings) {
			bindings = bindings + "<binding>" + binding + "</binding>";
		}
		bindings = bindings + "</bindings>";
		Representation rep = new StringRepresentation(bindings,
				MediaType.TEXT_PLAIN);
		getResponse().setEntity(rep);
		getResponse().setStatus(Status.SUCCESS_OK);
		return rep;
	}

}
