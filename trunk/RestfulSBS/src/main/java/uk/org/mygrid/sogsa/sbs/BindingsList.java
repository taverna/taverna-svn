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
	 * {@link SemanticBindingInstance} so the REST calls can get the {@link Map} of all
	 * the {@link Binding}s from the {@link SemanticBindingService}
	 * 
	 * @return
	 */
	protected synchronized void addBinding(String entityKey, String rdf) {
		((SemanticBindingService) getContext().getAttributes().get(
				Application.KEY)).addBinding(entityKey, rdf);

	}
//	protected boolean hasBinding(String key) {
//		return ((SemanticBindingService) getContext().getAttributes().get(
//				Application.KEY)).hasBinding(key);
//	}
	
	protected SemanticBindingInstance getBinding(String key) throws Exception {
		try {
			return ((SemanticBindingService) getContext().getAttributes().get(
					Application.KEY)).getBinding(key);
		} catch (SemanticBindingException e) {
			throw new Exception(e);
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
