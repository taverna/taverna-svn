package net.sf.taverna.service.rest.resources;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.Jobs;
import net.sf.taverna.service.xml.JobsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class QueueResource extends AbstractResource {

	private static final int MAX_SIZE = 16 * 1024;

	private static Logger logger = Logger.getLogger(QueueResource.class);
	
	private Queue queue;

	public QueueResource(Context context, Request request, Response response) {
		super(context, request, response);
		String queue_id = (String) request.getAttributes().get("queue");
		queue = daoFactory.getQueueDAO().read(queue_id);
		checkEntity(queue);
		addRepresentation(new URIList());
		addRepresentation(new XML());
	}

	class URIList extends AbstractURIList<Job> {
		@Override
		public Iterator<Job> iterator() {
			return queue.getJobs().iterator();
		}
	}

	class XML extends AbstractREST {
		@Override
		public XmlObject getXML() {
			JobsDocument doc = JobsDocument.Factory.newInstance();
			Jobs jobs = doc.addNewJobs();
			for (Job job : queue.getJobs()) {
				jobs.addNewJob().setHref(uriFactory.getURI(job));
			}
			return doc;
		}
	}
	
	@Override
	public long maxSize() {
		return MAX_SIZE;
	}
	
	@Override
	public Date getModificationDate() {
		return queue.getLastModified();
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public void post(Representation entity) {
		if (! isEntityValid(entity)) {
			return;
		}
		JobDocument doc;
		try {
			doc = JobDocument.Factory.parse(entity.getStream());
		} catch (XmlException ex) {
			logger.warn("Could not parse job document", ex);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Could not parse as XML");
			return;
		} catch (IOException ex) {
			logger.warn("Could not read job XML", ex);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			return;
		}
		
		String href = doc.getJob().getHref();
		if (href == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Job must be given by href");
			return;
		}
		Job job = uriToDAO.getResource(href, Job.class);
		if (job.getQueue().equals(this)) {
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		}
		if (job.getQueue() != null) {
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Job already on queue " + 
				uriFactory.getURI(job.getQueue()));
			return;
		}
		queue.addJob(job);
		daoFactory.commit();
		logger.info("Added " + job + " to " + queue);
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

	}


	
}
