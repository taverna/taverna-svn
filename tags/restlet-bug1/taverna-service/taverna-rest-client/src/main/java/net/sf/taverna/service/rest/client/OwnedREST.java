package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Owned;

import org.restlet.data.Reference;

public class OwnedREST<OwnedClass extends Owned> extends LinkedREST<OwnedClass> {

	public OwnedREST(RESTContext context, OwnedClass document) {
		super(context, document);
	}

	public OwnedREST(RESTContext context, Reference uri, OwnedClass document) {
		super(context, uri, document);
	}

	public OwnedREST(RESTContext context, Reference uri, Class<OwnedClass> documentClass) {
		super(context, uri, documentClass);
	}
	
	public UserREST getOwner() {
		if (getDocument().getOwner() == null) {
			return null;
		}
		return new UserREST(context, getDocument().getOwner());
	}
	

}