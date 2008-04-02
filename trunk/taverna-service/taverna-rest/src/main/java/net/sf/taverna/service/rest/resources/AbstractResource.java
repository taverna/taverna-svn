package net.sf.taverna.service.rest.resources;

import java.util.Date;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.AbstractBean;
import net.sf.taverna.service.datastore.bean.AbstractOwned;
import net.sf.taverna.service.datastore.bean.AbstractUUID;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.TavernaConstants;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.rest.utils.URItoDAO;
import net.sf.taverna.service.xml.Data;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public abstract class AbstractResource extends RepresentationalResource {

	private static Logger logger = Logger.getLogger(AbstractResource.class);

	public static final MediaType restType =
		new MediaType(TavernaConstants.restType);

	public static final MediaType scuflType =
		new MediaType(TavernaConstants.scuflType);

	public static final MediaType baclavaType =
		new MediaType(TavernaConstants.baclavaType);
	
	public static final MediaType reportType =
		new MediaType(TavernaConstants.reportType);
	
	public static final MediaType consoleType =
		new MediaType(TavernaConstants.consoleType);

	protected DAOFactory daoFactory = DAOFactory.getFactory();

	URIFactory uriFactory;

	URItoDAO uriToDAO;

	public AbstractResource(Context context, Request request, Response response) {
		super(context, request, response);
		uriFactory = URIFactory.getInstance();
		uriToDAO = URItoDAO.getInstance(uriFactory);
	}
	
	/**
	 * The last modification date of the resource, if known
	 * 
	 * @return
	 */
	public Date getModificationDate() {
		return null;
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Representation result = super.getRepresentation(variant);
		if (result == null) {
			logger.warn("No representation produced for " + this);
			return null;
		}
		if (result.getModificationDate() == null
			&& getModificationDate() != null) {
			result.setModificationDate(getModificationDate());
		}
		return result;
	}

	/**
	 * Maximum size for PUT/POST. Checked by
	 * {@link #overMaxSize(Representation)}. If the size is less than 0 (say
	 * -1, the default), {@link #overMaxSize(Representation)} will always return
	 * false.
	 * 
	 * @see #overMaxSize(Representation)
	 * @return Maximum size as a number of bytes
	 */
	public long maxSize() {
		return -1;
	}

	/**
	 * Check if an uploaded entity is too large to be handled by this resource.
	 * Return true if the entity is larger than {@link #maxSize()}. If
	 * {@link #maxSize()} returns a number less than 0, this function always
	 * return false.
	 * 
	 * @see #maxSize()
	 * @param entity
	 *            Entity which size is to be checked
	 * @return true if the entity was larger than #maxSize()
	 */
	public boolean overMaxSize(Representation entity) {
		if (maxSize() < 0) {
			return false;
		}
		if (entity.getSize() > maxSize()) {
			logger.warn("Submitted representation (" + entity.getSize() + 
					" bytes) was above maximum size (" + maxSize() + " bytes)");
			getResponse().setStatus(Status.SERVER_ERROR_INSUFFICIENT_STORAGE,
				"Maximum size is " + maxSize());
			return true;
		}
		return false;
	}

	/**
	 * Set the response status to {@value Status#CLIENT_ERROR_NOT_FOUND}
	 */
	public void notFound() {
		logger.info("Not found for " + this);
		getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
	}

	/**
	 * Set the response status to {@value Status#CLIENT_ERROR_UNAUTHORIZED} with
	 * the appropriate challenge to request new authentication.
	 */
	public void challenge() {
		logger.info("Challenging for " + this);
		getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		getResponse().setChallengeRequest(
			new ChallengeRequest(UserGuard.SCHEME, UserGuard.REALM));
	}

	/**
	 * Check existance and authorized access on entity. Challenges if required.
	 * Return true of entity exists and is accessible, otherwise false. On false
	 * return, the status of the request will have been set to either
	 * {@value Status#CLIENT_ERROR_NOT_FOUND} or
	 * {@value Status#CLIENT_ERROR_UNAUTHORIZED}
	 * 
	 * @see #notFound()
	 * @see #challenge()
	 * @see #isEntityAuthorized(AbstractUUID)
	 * @param entity
	 * @return True if entity exist and is accessible.
	 */
	public boolean checkEntity(AbstractBean entity) {
		logger.debug("Checking entity " + entity);
		if (entity == null) {
			notFound();
			logger.debug("Entity was null");
			return false;
		}
		if (!isEntityAuthorized(entity)) {
			logger.debug("Entity was not authorized: " + entity);
			challenge();
			return false;
		}
		return true;
	}

	public boolean checkIsAdmin() {
		User authUser = getAuthUser();
		if (authUser.isAdmin()) {
			logger.debug("Authenticated user " + authUser + " was admin");
			return true;
		}
		challenge();
		return false;
	}

	public User getAuthUser() {
		return (User) getRequest().getAttributes().get(
			UserGuard.AUTHENTICATED_USER);
	}

	/**
	 * Check if the currently authenticated used, as injected by
	 * {@link UserGuard}, is authorized to access the entity.
	 * 
	 * @param entity
	 * @return True if entity access is authorized.
	 */
	public boolean isEntityAuthorized(AbstractBean entity) {
		User authUser = getAuthUser();
		if (authUser!=null && authUser.isAdmin()) {
			logger.debug("Authorized admin " + authUser + " for accessing " + entity);
			return true;
		}
		
		if (entity instanceof User) {
			logger.debug("Comparing " + entity + " with " + authUser);
			// Users can access their own user
			if (entity.equals(authUser))
				logger.debug("Authorized user " + authUser + " for accessing itself");
				return true;
		}
		if (entity instanceof AbstractOwned) {
			// Owned resources only readable by owner
			AbstractOwned owned = (AbstractOwned) entity;
			if (owned.getOwner() == null) {
				logger.debug("Not owned resource, access granted");
				// No owner, also readable by all
				return true;
			}
			logger.debug("Comparing owner " + owned.getOwner() + " with "
					+ authUser);
			if (owned.getOwner().equals(authUser)) {
				logger.debug("Owner user " + authUser
						+ " authorised for accessing " + owned);
				return true;
			}
		} else {
			if (!(entity instanceof User)) { // anything, other than User,
												// that is not owned should be
												// accessible
				logger.debug("Authorised access to not ownable" + entity);
				return true;
			}
		}
		
		if (authUser instanceof Worker
			&& isWorkerAuthorized((Worker) authUser, entity)) {
			logger.debug("Authorized worker " + authUser + " to " + entity);
			return true;
		}
		if (entity instanceof Queue) {
			if (entity.equals(daoFactory.getQueueDAO().defaultQueue())) {
				logger.debug("Authorized to default queue " + entity);
				return true;
			} else {
				logger.warn("Can't check access on non-default queue");
				// TODO: Implement ACLs on other queues
			}
		}
		logger.debug("No access to " + entity + " for " + authUser);
		return false;

	}


	private boolean isWorkerAuthorized(Worker worker, AbstractBean entity) {
		//refresh the worker to ensure the list of worker jobs is up to date.
		worker = daoFactory.getWorkerDAO().refresh(worker);

		if (entity instanceof Queue) {
			Queue queue = (Queue) entity;
			boolean authorized = queue.getWorkers().contains(worker);
			if (authorized) {
				logger.debug("Authorized worker " + worker + " access to queue " + entity);
			}
			return authorized;
		}
		if (entity instanceof Job) {
			Job job = (Job) entity;
			if (job.getWorker() == null) {
				if (worker.getQueue().hasJob(job)) {
					logger.debug("Authorized worker " + worker + " access to queued job " + job);
					return true;
				}
			}
			if (worker.equals(job.getWorker())) {
				logger.debug("Authorized worker " + worker + " access to assigned job " + job);
				return true;
			}
		}
		if (entity instanceof Workflow) {
			for (Job job : worker.getWorkerJobs()) {
				if (entity.equals(job.getWorkflow())) {
					logger.debug("Authorized worker " + worker
							+ " access to workflow " + entity
							+ " from assigned job " + job);
					return true;
				}
			}
		}

		if (entity instanceof User) {
			for (Job job : worker.getWorkerJobs()) {
				if (entity.equals(job.getOwner())) {
					logger.debug("Authorized worker " + worker
							+ " access to assigned job's owner " + entity);
					return true;
				}
			}
		}

		if (entity instanceof DataDoc) {
			for (Job job : worker.getWorkerJobs()) {
				if (entity.equals(job.getInputs())) {
					logger.debug("Authorized worker " + worker + " access to inputs of job " + job);

					return true;
				}
				if (entity.equals(job.getOutputs())) {
					logger.debug("Authorized worker " + worker + " access to outputs of job " + job);
					return true;
				}
			}
		}
		logger.info("Worker authorization failed; worker=" + worker
			+ ", entity=" + entity);
		return false;
	}

	/**
	 * Check if an entity submitted for {@link #post(Representation)} and
	 * {@link #put(Representation)} is of the right size of the content type
	 * {@value #restType}.
	 * <p>
	 * This method is basically the same as
	 * {@link #isEntityValid(Representation, MediaType)} with media type
	 * {@link #restType}.
	 * 
	 * @see #isEntityValid(Representation, MediaType)
	 * @param entity
	 *            The incoming entity to verify
	 * @return <code>true</code> if the entity is valid.
	 */
	public boolean isEntityValid(Representation entity) {
		return isEntityValid(entity, restType);
	}

	/**
	 * Check if an entity submitted for {@link #post(Representation)} and
	 * {@link #put(Representation)} is of the right size and content type.
	 * <p>
	 * The maximum size is determined by {@link #overMaxSize(Representation)}.
	 * <p>
	 * If the entity is not valid, the correct status code is set on the
	 * response and <code>false</code> is returned.
	 * 
	 * @param entity
	 *            The incoming entity to verify
	 * @return <code>true</code> if the entity is valid.
	 */
	public boolean isEntityValid(Representation entity, MediaType mediaType) {
		MediaType entityMediaType = entity.getMediaType();
		if (!mediaType.includes(entityMediaType)) {
			logger.warn("Content type was " + entityMediaType
					+ " but should be " + mediaType);
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + mediaType);
			return false;
		}
		if (overMaxSize(entity)) {
			logger.warn("Uploaded entity was too large: "
				+ entity.getSize());
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		// Classname and relative URI
		return getClass().getSimpleName() + " " + getRequest().getResourceRef();
	}

	/**
	 * A Taverna-service XMLBeans representation. The mime type is set to
	 * {@link AbstractResource#restType}.
	 */
	public abstract class AbstractREST<ElementType extends XmlObject> extends AbstractXML {
		
		public ElementType element;
		
		/**
		 * Create and return the document. The document should also have it's
		 * root element added and assigned to the member {@link #element}.
		 * 
		 * @return
		 */
		public abstract XmlObject createDocument();
		
		
		/**
		 * Add sub-elements to the root element (or any of its children). This
		 * will normally be the method which adds information to the document,
		 * so that each class in the hierarchy can add its information, and
		 * <code>super.addElements()</code> to add the rest.
		 * 
		 * @param element
		 *            The element to which to add information, normally the same
		 *            as {@link #element}.
		 */
		public abstract void addElements(ElementType element);
		
		/**
		 * Generate the XML document by creating the root with
		 * {@link #createDocument()} and adding elements using
		 * {@link #addElements(XmlObject)}.
		 */
		@Override
		public XmlObject getXML() {
			XmlObject doc = createDocument();
			addElements(element);
			return doc;
		}
		
		@Override
		public MediaType getMediaType() {
			return restType;

		}
	}

	/**
	 * An <code>text/uri-list</code> over UUIDResource's. The URIs will be
	 * generated using {@link URIFactory}.
	 * 
	 * @see ReferenceList
	 */
	public abstract class AbstractURIList<ResourceType extends AbstractUUID> extends
		AbstractText implements Iterable<ResourceType> {

		abstract public Iterator<ResourceType> iterator();

		@Override
		public String getText() {
			StringBuilder message = new StringBuilder();
			
			for (ResourceType resource : this) {
				message.append(uriFactory.getURI(resource)).append("\r\n");
			}
			return message.toString();
		}

		@Override
		public MediaType getMediaType() {
			return MediaType.TEXT_URI_LIST;
		}
	}
	


}
