package uk.org.mygrid.sogsa.sbs;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class QueryBinding extends BindingsList {

	private String key;
	private SemanticBindingInstance binding;

	public QueryBinding(Context context, Request request, Response response) {
		super(context, request, response);
		this.key = (String) getRequest().getAttributes().get("binding");

		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "key: " + key);

		// Check if this binding actually exists
		try {
			if (getBinding(key) != null) {
				this.binding = getBinding(key);
				getVariants().add(new Variant(MediaType.TEXT_XML));
			} else {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, "There was no binding to get");
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, "There was a problem with the request");
		}
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void post(Representation entity) {
		Form form = new Form(entity);
		String query = form.getFirstValue("query");
		String queryBinding = queryBinding(this.key, query);
		if (queryBinding != null) {
			getResponse().setStatus(Status.SUCCESS_OK);
			// TODO put query in the rep
			Representation rep = new StringRepresentation(queryBinding,
					MediaType.TEXT_PLAIN);
			// Indicates where is located the new resource.
			rep.setIdentifier(getRequest().getResourceRef().getIdentifier());
			getResponse().setEntity(rep);
			
		} else {
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			// TODO put query in the rep
			Representation rep = new StringRepresentation("No query results returned",
					MediaType.TEXT_PLAIN);
			// Indicates where is located the new resource.
			rep.setIdentifier(getRequest().getResourceRef().getIdentifier());
			getResponse().setEntity(rep);
			
		}
	}

}
