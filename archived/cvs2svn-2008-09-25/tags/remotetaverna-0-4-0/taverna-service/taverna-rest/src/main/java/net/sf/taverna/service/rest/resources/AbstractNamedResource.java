package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.AbstractNamed;
import net.sf.taverna.service.xml.Named;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractNamedResource<NamedClass extends AbstractNamed>
	extends AbstractDatedResource<NamedClass> {

	public AbstractNamedResource(Context context, Request request,
		Response response) {
		super(context, request, response);
	}

	public abstract class AbstractNamedREST<NamedType extends Named> extends
		AbstractDatedREST<NamedType> {

		@Override
		public void addElements(NamedType element) {
			super.addElements(element);
			if (getResource().getName() != null) {
				element.setTitle(getResource().getName());
			}
		}
	}

}
