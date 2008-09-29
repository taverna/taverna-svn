package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.AbstractOwned;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.xml.Owned;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractOwnedResource<OwnedClass extends AbstractOwned>
	extends AbstractNamedResource<OwnedClass> {

	public AbstractOwnedResource(Context context, Request request,
		Response response) {
		super(context, request, response);
	}

	public abstract class AbstractOwnedXML<OwnedType extends Owned> extends
		AbstractNamedREST<OwnedType> {

		@Override
		public void addElements(OwnedType element) {
			super.addElements(element);
			User owner = getResource().getOwner();
			if (owner != null) {
				net.sf.taverna.service.xml.User ownerElem =
					element.addNewOwner();
				ownerElem.setHref(uriFactory.getURIUser(owner));
				ownerElem.setUsername(owner.getUsername());
			}
		}
	}

}
