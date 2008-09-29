package net.sf.taverna.service.rest.resources;

import java.util.Date;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.OwnedResource;
import net.sf.taverna.service.datastore.bean.UUIDResource;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.rest.utils.URItoDAO;

import org.apache.log4j.Logger;
import org.jdom.Namespace;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public abstract class AbstractResource extends RepresentationalResource {

	private static Logger logger = Logger.getLogger(AbstractResource.class);

	public static final Namespace ns =
		Namespace.getNamespace(TavernaService.NS);

	public static final Namespace nsDC =
		Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");

	public static final Namespace nsDCterms =
		Namespace.getNamespace("dcterms", "http://purl.org/dc/terms/");

	public static final MediaType restType =
		new MediaType(TavernaService.restType);

	public static final MediaType scuflType =
		new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType =
		new MediaType(TavernaService.baclavaType);

	static DAOFactory daoFactory = DAOFactory.getFactory();

	static URIFactory uriFactory = URIFactory.getInstance();

	static URItoDAO uriToDAO = URItoDAO.getInstance();

	public AbstractResource(Context context, Request request, Response response) {
		super(context, request, response);
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
	 * @see #isEntityAuthorized(UUIDResource)
	 * @param entity
	 * @return True if entity exist and is accessible.
	 */
	public boolean checkEntity(UUIDResource entity) {
		logger.debug("Checking entity " + entity);
		if (entity == null) {
			notFound();
			return false;
		}
		if (!isEntityAuthorized(entity)) {
			challenge();
			return false;
		}
		return true;
	}

	/**
	 * Check if the currently authenticated used, as injected by
	 * {@link UserGuard}, is authorized to access the entity.
	 * 
	 * @param entity
	 * @return True if entity access is authorized.
	 */
	public boolean isEntityAuthorized(UUIDResource entity) {
		User authUser =
			(User) getContext().getAttributes().get(
				UserGuard.AUTHENTICATED_USER);
		if (entity instanceof User) {
			logger.info("Comparing " + entity + " with " + authUser);
			// Users can access their own user
			return entity.equals(authUser);
		}
		if (!(entity instanceof OwnedResource)) {
			logger.info("Not ownable resource, access granted");
			// All non-owned resources are free to access
			// (Unless an overloaded version of this method says otherwise)
			return true;
		}
		// Owned resources only readable by owner
		OwnedResource owned = (OwnedResource) entity;
		if (owned.getOwner() == null) {
			logger.info("Not owned resource, access granted");
			// No owner, also readable by all
			return true;
		}
		logger.info("Comparing owner " + owned.getOwner() + " with " + authUser);
		return (owned.getOwner().equals(authUser));
		// TODO: Workers should be able to access things that are on their
		// queues,
		// in particular worker w should be able to access:
		// /queue/Q if Q has worker w
		// /job/J if J is in queue/Q
		// /workflow/WF if WF is used by J
		// /data/D if J has i/o to D
	}

	/**
	 * A Taverna-service XMLBeans representation. The mime type is set to
	 * {@link AbstractResource#restType}.
	 */
	abstract class AbstractREST extends AbstractXML {
		@Override
		public MediaType getMediaType() {
			return restType;

		}
	}

	/**
	 * An <code>text/uri-list</code> over UUIDResource's. The URIs will be
	 * generated using {@link URIFactory}.
	 */
	abstract class AbstractURIList<ResourceType extends UUIDResource> extends
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
