package uk.org.mygrid.sogsa.sbs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openanzo.common.exceptions.AnzoException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 * Contains method to get at the {@link Map} of all the {@link Binding}s
 * 
 * @author Ian Dunlop
 * 
 */
public class BindingsList extends Resource {

	public BindingsList(Context context, Request request, Response response) {
		super(context, request, response);
	}

	/**
	 * Convencience class shared between the {@link SemanticBindings} and
	 * {@link SemanticBindingInstance} so the REST calls can get the {@link Map}
	 * of all the {@link Binding}s from the {@link SemanticBindingService}
	 * 
	 * @return
	 * @throws Exception 
	 */
	protected synchronized void addBinding(String entityKey, String rdf) throws Exception {
	
			try {
				((SemanticBindingService) getContext().getAttributes().get(
						Application.KEY)).addBinding(entityKey, rdf);
			} catch (RDFHandlerException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				throw new Exception(e);
			} catch (RDFParseException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				throw new Exception(e);
			} catch (SQLException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				throw new Exception(e);
			} catch (IOException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				throw new Exception(e);
			} catch (SemanticBindingException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				throw new Exception(e);
			} catch (AnzoException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				throw new Exception(e);
			}
		

	}

	// protected boolean hasBinding(String key) {
	// return ((SemanticBindingService) getContext().getAttributes().get(
	// Application.KEY)).hasBinding(key);
	// }

	/**
	 * Find a binding in the database with the matching key provided.
	 */
	protected SemanticBindingInstance getBinding(String key) throws Exception {
		try {
			return ((SemanticBindingService) getContext().getAttributes().get(
					Application.KEY)).getBinding(key);
		} catch (SemanticBindingNotFoundException e) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			throw e;
		} catch (AnzoException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			throw e;
		} catch (NoRDFFoundException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			throw e;
		}
	}

	protected Iterable<URI> getAllBindings() throws SemanticBindingException,
			SemanticBindingNotFoundException {
		try {
			return ((SemanticBindingService) getContext().getAttributes().get(
					Application.KEY)).getAllBindings();
		} catch (SemanticBindingException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			throw e;
		} catch (SemanticBindingNotFoundException e) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			throw e;
		}
	}

	protected void removeBinding(String key) {
		((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).removeBinding(key);
	}

	protected void updateRDF(String key, String rdf) throws Exception {
		try {
			((SemanticBindingService) getContext().getAttributes().get(
					Application.KEY)).updateRDF(key, rdf);
		} catch (SemanticBindingException e) {

			throw new Exception(e);
		}
	}

	protected String queryBinding(String key, String query) {
		return ((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).queryBinding(key, query);
	}

}
