package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.DataDocument;
import net.sf.taverna.service.xml.Owned;
import net.sf.taverna.service.xml.User;

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
		
	/**
	 * Return the owner, or <code>null</code> if the resource does not have an
	 * owner.
	 */
	public UserREST getOwner() {
		User owner = getDocument().getOwner();
		if (owner == null) {
			return null;
		}
		return new UserREST(context, owner);
	}
	
	/**
	 * Set the owner of the resource, or set to <code>null</code> if the
	 * resource is to have no owner.
	 * 
	 * @param owner The {@link UserREST} which is to be the owner
	 * @throws NotSuccessException
	 */
	public void setOwner(UserREST owner) throws NotSuccessException {
		DataDocument dataDoc = DataDocument.Factory.newInstance();
		User ownerElem = dataDoc.addNewData().addNewOwner();
		if (owner != null) {
			ownerElem.setHref(owner.getURI());
		}
		context.put(getURIReference(), dataDoc);
		invalidate();
	}
	

}