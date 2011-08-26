package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.xml.Capabilities;
import net.sf.taverna.service.xml.CapabilitiesDocument;

import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class CapabilitiesResource extends AbstractResource {

	public CapabilitiesResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		addRepresentation(new CapabilitiesVelocityRepresentation());
		addRepresentation(new XML());
		
	}

	public class XML extends AbstractREST<Capabilities> {
		@Override
		public XmlObject createDocument() {
			CapabilitiesDocument doc =
				CapabilitiesDocument.Factory.newInstance();
			element = doc.addNewCapabilities();
			return doc;
		}

		@Override
		public void addElements(Capabilities capabilities) {
			capabilities.addNewQueues().setHref(uriFactory.getURI(Queue.class));
			capabilities.addNewUsers().setHref(uriFactory.getURI(User.class));
			capabilities.addNewCurrentUser().setHref(
				uriFactory.getURICurrentUser());
			capabilities.addNewWorkers().setHref(
				uriFactory.getURI(Worker.class));
			capabilities.addNewDefaultQueue().setHref(uriFactory.getURIDefaultQueue());
		}
	}
	
	class CapabilitiesVelocityRepresentation extends VelocityRepresentation {
	
		@Override
		protected String pageTitle() {
			return "Remote Taverna Execution";
		}

		@Override
		protected String templateName() {
			return "capabilities.vm";
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("users", uriFactory.getURI(User.class));
			model.put("currentUser", uriFactory.getURICurrentUser());
			model.put("queues", uriFactory.getURI(Queue.class));
			model.put("defaultQueue", uriFactory.getURIDefaultQueue());
			model.put("workers", uriFactory.getURI(Worker.class));
			return model;
		}
	}

}
