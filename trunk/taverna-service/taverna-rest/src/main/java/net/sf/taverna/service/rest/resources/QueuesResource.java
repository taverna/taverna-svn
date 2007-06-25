package net.sf.taverna.service.rest.resources;

import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.xml.Queues;
import net.sf.taverna.service.xml.QueuesDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class QueuesResource extends AbstractResource {
	private static Logger logger = Logger.getLogger(QueuesResource.class);

	
	public QueuesResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new URIList());
		addRepresentation(new XML());
	}
	
	class URIList extends AbstractURIList<Queue> {
		@Override
		public Iterator<Queue> iterator() {
			return daoFactory.getQueueDAO().iterator();
		}
	}

	class XML extends AbstractREST {
		@Override
		public XmlObject getXML() {
			QueuesDocument doc = QueuesDocument.Factory.newInstance();
			Queues queues = doc.addNewQueues();
			for (Queue q : daoFactory.getQueueDAO()) {
				queues.addNewQueue().setHref(uriFactory.getURI(q));
			}
			return doc;
		}
	}
	
	@Override
	public void post(Representation entity) {
		if (! checkIsAdmin()) {
			return;
		}
		// Don't care about the content
		Queue q = new Queue();
		daoFactory.getQueueDAO().create(q);
		daoFactory.commit();
		logger.info("Created " + q);
		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setRedirectRef(uriFactory.getURI(q));
	}
	
}
