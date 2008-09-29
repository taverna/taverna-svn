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

public class QueryAllBindings extends BindingsList{

	public QueryAllBindings(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
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
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, queryBinding);
				getResponse().setStatus(Status.SUCCESS_OK);
				Representation rep = new StringRepresentation(queryBinding,
						MediaType.TEXT_PLAIN);
				getResponse().setEntity(rep);

			} else {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, "No contents in all query");
				getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
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
	

}
