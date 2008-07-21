package uk.org.mygrid.sogsa.sbs;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
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
	 * {@link SemanticBinding} so the REST calls can get the {@link Map} of all
	 * the {@link Binding}s from the {@link SemanticBindingService}
	 * 
	 * @return
	 */
	protected synchronized void addBinding(String key, String rdf) {
		((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).addBinding(key, rdf);

	}
	protected boolean hasBinding(String key) {
		return ((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).hasBinding(key);
	}
	
	protected Binding getBinding(String key) {
		return ((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).getBinding(key);
	}
	

}
