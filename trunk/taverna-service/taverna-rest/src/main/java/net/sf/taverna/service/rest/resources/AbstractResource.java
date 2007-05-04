package net.sf.taverna.service.rest.resources;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.OwnedResource;
import net.sf.taverna.service.datastore.bean.UUIDResource;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.XMLUtils;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.jdom.Element;
import org.jdom.Namespace;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public abstract class AbstractResource extends Resource {

	URIFactory uriFactory = URIFactory.getInstance();

	public XmlOptions xmlOptions = makeXMLOptions();

	public static final Namespace ns =
		Namespace.getNamespace(TavernaService.NS);

	public static final Namespace nsDC =
		Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");

	public static final Namespace nsDCterms =
		Namespace.getNamespace("dcterms", "http://purl.org/dc/terms/");
	
	private static Logger logger = Logger.getLogger(AbstractResource.class);
	
	public static final MediaType restType = new MediaType(TavernaService.restType);

	public static final MediaType scuflType = new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType = new MediaType(TavernaService.baclavaType);
	
	
	static DAOFactory daoFactory = DAOFactory.getFactory();

	public AbstractResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
		getVariants().add(new Variant(restType));
	}

	private static XmlOptions makeXMLOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setLoadStripWhitespace();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(4);
		xmlOptions.setSaveOuter();
		xmlOptions.setUseDefaultNamespace();
		xmlOptions.setSaveAggressiveNamespaces();
		Map<String, String> ns = new HashMap<String, String>();
		ns.put("http://www.w3.org/1999/xlink", "xlink");
		xmlOptions.setSaveSuggestedPrefixes(ns);
		return xmlOptions;
	}

	public Date getModificationDate() {
		return null;
	}

	/**
	 * Return a plain text representation. There is no real restrictions on the
	 * plain text except that it should be both human-readable and easily
	 * parsable. A RFC-822 style is often convenient, for example:
	 * 
	 * <pre>
	 *  Name: Stian Soiland
	 *  Address: Manchester
	 *           United Kingdom
	 *  Homepage: http://soiland.no/
	 * </pre>
	 * 
	 * @return A plain text representation of the resource
	 */
	public abstract String representPlainText();

	/**
	 * Return XML representation as String. By default this method will call
	 * {@link #representXMLElement()} and return the serialised element.
	 * <p>
	 * The motivation for this design is that if you have the XML representation
	 * already as pure XML in a file or database, this method can return it
	 * directly. However, if you are building your own XML {@link Element} from
	 * scratch, then {@link #representXMLElement()} would be more convenient.
	 * 
	 * @return An XML representation of the resource.
	 */
	public String representXML() {
		Element element = representXMLElement();
		return XMLUtils.makeXML(element);
	}

	/**
	 * Return XML representation as an Element. This method is called by
	 * {@link #representXML()} by default.
	 * 
	 * @see #representXML()
	 * @return A root element for the XML representation of the resource
	 */
	public Element representXMLElement() {
		return null;
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Representation result = null;
		if (restType.includes(variant.getMediaType())) {
			result = new StringRepresentation(representXML(), restType);
		} else { // Fall-back to text/plain
			if (!MediaType.TEXT_PLAIN.includes(variant.getMediaType())) {
				logger.warn("Unknown media type " + variant.getMediaType());
			}
			result = new StringRepresentation(representPlainText());
		}
		if (getModificationDate() != null) {
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
	 * Set the response status to  {@value Status#CLIENT_ERROR_NOT_FOUND}
	 *
	 */
	public void notFound() {
		logger.info("Not found for " + this);
		getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
	}

	/**
	 * Set the response status to {@value Status#CLIENT_ERROR_UNAUTHORIZED} with
	 * the appropriate challenge to request new authentication. 
	 * 
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
			logger.info("Not ownned resource, access granted");
			// No owner, also readable by all
			return true;
		}
		logger.info("Comparing owner " + owned.getOwner() + " with " + authUser);
		return (owned.getOwner().equals(authUser));
		// TODO: Workers should be able to access things that are on their queues, 
		// in particular worker w should be able to access:
		//    /queue/Q if Q has worker w
		//    /job/J if J is in queue/Q
		//    /workflow/WF if WF is used by J
		//    /data/D if J has i/o to D
	}
	
}
