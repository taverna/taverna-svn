package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.xml.Capabilities;
import net.sf.taverna.service.xml.CapabilitiesDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class CapabilitiesResource extends AbstractResource {

	public CapabilitiesResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		addRepresentation(new XML());
	}

	private static Logger logger = Logger.getLogger(CapabilitiesResource.class);

	public class XML extends AbstractREST {

		@Override
		public XmlObject getXML() {
			CapabilitiesDocument capDoc =
				CapabilitiesDocument.Factory.newInstance();
			Capabilities capabilities = capDoc.addNewCapabilities();
			capabilities.addNewQueues().setHref(uriFactory.getURI(Queue.class));
			capabilities.addNewUsers().setHref(uriFactory.getURI(User.class));
			capabilities.addNewCurrentUser().setHref(
				uriFactory.getURICurrentUser());
			capabilities.addNewWorkers().setHref(
				uriFactory.getURI(Worker.class));
			return capDoc;
		}

	}

}
