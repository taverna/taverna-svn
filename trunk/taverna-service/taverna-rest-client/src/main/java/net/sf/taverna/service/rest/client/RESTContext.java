package net.sf.taverna.service.rest.client;

import java.io.IOException;
import java.util.UUID;

import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.xml.Capabilities;
import net.sf.taverna.service.xml.CapabilitiesDocument;
import net.sf.taverna.service.xml.UserDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.jdom.Element;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

public class RESTContext {

	public static final MediaType restType =
		new MediaType(TavernaService.restType);

	public static final MediaType scuflType =
		new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType =
		new MediaType(TavernaService.baclavaType);

	private static Logger logger = Logger.getLogger(RESTContext.class);

	private String name;

	private Reference baseURI;

	private String username;

	private String password;

	private UserREST user;

	private WorkersREST workers;

	private Reference usersURI;

	private Reference workersURI;

	private Reference currentUserURI;

	private Reference queuesURI;

	private Capabilities capabilities;

	public static RESTContext register(String baseURI) {
		return register(UUID.randomUUID().toString());
	}
	
	/**
	 * Register a user and return a {@link RESTContext} for that user.
	 * 
	 * @param baseURI Base URI of service
	 * @param username Desired username
	 * @return An initialized {@link RESTContext}
	 * @throws NotSuccessException
	 */
	public static RESTContext register(String baseURI, String username)
		throws NotSuccessException {
		
		RESTContext anonContext = new RESTContext(baseURI);
		Reference uri = anonContext.getUsersURI();
		
		UserDocument userDoc = UserDocument.Factory.newInstance();
		userDoc.addNewUser().setUsername(username);

		Response response = anonContext.post(uri, userDoc);
		if (!response.getStatus().isSuccess()) {
			throw new NotSuccessException(response.getStatus());
		}
		logger.info("Registered " + username + " at " + baseURI);
		String password;
		try {
			password = response.getEntity().getText();
			logger.debug("Password: " + password);
		} catch (IOException e) {
			logger.warn("Could not read password", e);
			throw new RuntimeException(
				"Could not read password of generated user");
		}
		return new RESTContext(baseURI, username, password);
	}
	
	/**
	 * (Private) constructor for an anonymous connection
	 * 
	 * @param baseURI
	 */
	private RESTContext(String baseURI) {
		this.baseURI = new Reference(baseURI);
	}

	public RESTContext(String baseURI, String username, String password) {
		this(baseURI);
		if (baseURI == null || username == null || password == null) {
			throw new NullPointerException(
				"uri/username/password can't be null");
		}

		this.username = username;
		this.password = password;
	}

	/**
	 * Construct a {@link RESTContext} from a serialised XML element containing
	 * the URI, username and password.
	 * 
	 * @see #toXML()
	 * @param restContext
	 *            An element containing the children elements "uri", "username"
	 *            and "password"
	 * @return An initialised {@link RESTContext}
	 */
	public static RESTContext fromXML(Element restContext) {
		String uri = restContext.getChildText("uri");
		String username = restContext.getChildText("username");
		String password = restContext.getChildText("password");
		RESTContext context = new RESTContext(uri, username, password);
		String name = restContext.getChildText("name");
		if (name != null) {
			context.setName(name);
		}
		return context;
	}

	/**
	 * Serialise the RESTContext as a simple XML element "restContext"
	 * containing three children, "uri", "username" and "password.
	 * 
	 * @see #fromXML(Element)
	 * @return An XML serialised {@link Element} of the {@link RESTContext}.
	 */
	public Element toXML() {
		Element e = new Element("restContext");
		if (name != null) {
			e.addContent(new Element("name").addContent(name));
		}
		e.addContent(new Element("uri").addContent(baseURI.toString()));
		e.addContent(new Element("username").addContent(username));
		e.addContent(new Element("password").addContent(password));
		return e;
	}

	public Response head(Reference uri) throws NotSuccessException {
		return request(Method.HEAD, uri, null, null, false);
	}

	public Response get(Reference uri) throws NotSuccessException,
		MediaTypeException {
		return get(uri, restType);
	}

	public Response get(Reference uri, MediaType accepts)
		throws NotSuccessException, MediaTypeException {
		Response response = request(Method.GET, uri, null, null, accepts);
		MediaType mediaType = response.getEntity().getMediaType();
		if (!accepts.includes(mediaType)) {
			throw new MediaTypeException(accepts, mediaType);
		}
		return response;
	}

	public Response post(Reference uri, String data, MediaType mediaType)
		throws NotSuccessException {
		return request(Method.POST, uri, data, mediaType, null);
	}

	public Response post(Reference uri, ReferenceList urls)
		throws NotSuccessException {
		return request(Method.POST, uri, urls.getTextRepresentation(), null,
			true);
	}

	public Response put(Reference uri, String data, MediaType mediaType)
		throws NotSuccessException {
		return request(Method.PUT, uri, data, mediaType, null);
	}

	public Response put(Reference uri, ReferenceList urls)
		throws NotSuccessException {
		return request(Method.PUT, uri, urls.getTextRepresentation(), null,
			true);
	}

	public Response delete(Reference uri) throws NotSuccessException {
		return request(Method.DELETE, uri, null, null, null);
	}

	public Reference getBaseURI() {
		return baseURI;
	}

	public Response post(Reference uri, XmlObject document)
		throws NotSuccessException {
		// FIXME: Should stream the XML and use document.save()
		return post(uri, document.xmlText(), restType);
	}
	
	public Response put(Reference uri, XmlObject document)
		throws NotSuccessException {
		// FIXME: Should stream the XML and use document.save()
		return put(uri, document.xmlText(), restType);
	}

	public UserREST getUser() throws NotSuccessException {
		if (user == null) {
			// Find current user through redirect
			// TODO: Support data directly at currentUserURI?
			Response response = head(getCurrentUserURI());
			user = new UserREST(this, response.getRedirectRef().getTargetRef());
		}
		return user;
	}

	public WorkersREST getWorkers() {
		if (workers == null) {
			workers = new WorkersREST(this, getWorkersURI());
		}
		return workers;
	}

	public Reference makeReference(String uri) {
		return new Reference(getBaseURI(), uri);
	}

	public <DocumentClass extends XmlObject> DocumentClass loadDocument(
		String uri, Class<DocumentClass> documentClass) {
		return loadDocument(new Reference(uri), documentClass);
	}

	public <DocumentClass extends XmlObject> DocumentClass loadDocument(
		Reference uri, Class<DocumentClass> documentClass) {
		Response response;
		try {
			response = get(uri);
		} catch (RESTException e) {
			logger.error("Could not get document from " + uri, e);
			throw new RuntimeException("Could not get document from " + uri, e);
		}
		try {
			XmlObject document =
				XmlObject.Factory.parse(response.getEntity().getStream());
			logger.info("Retrieved " + document);
			if (!documentClass.isInstance(document)
				&& document.schemaType().isDocumentType()) {
				// Extract the "inner" root element instead of the document
				// ie. can be cast to User instead of UserDocument
				XmlObject[] children = document.selectChildren(QNameSet.ALL);
				document = children[0];
			}
			if (!documentClass.isInstance(document)) {
				logger.error("Not a valid " + documentClass.getCanonicalName()
					+ ": " + uri);
				throw new RuntimeException("Could not parse as "
					+ documentClass.getCanonicalName());
			}
			return documentClass.cast(document);
		} catch (XmlException ex) {
			logger.warn("Could not parse user XML from " + uri, ex);
			throw new RuntimeException("Could not parse document XML from "
				+ uri, ex);
		} catch (IOException ex) {
			logger.warn("Could not read user XML from " + uri, ex);
			throw new RuntimeException("Could not read document XML from "
				+ uri, ex);
		}
	}

	private synchronized void checkCapabilities() {
		if (capabilities != null) {
			return;
		}
		capabilities =
			loadDocument(getBaseURI(), CapabilitiesDocument.class).getCapabilities();
		usersURI = makeReference(capabilities.getUsers().getHref());
		currentUserURI = makeReference(capabilities.getCurrentUser().getHref());
		workersURI = makeReference(capabilities.getWorkers().getHref());
		queuesURI = makeReference(capabilities.getQueues().getHref());
	}

	private Response request(Method method, Reference uri,
		Representation representation, MediaType accepts, boolean checkSuccess)
		throws NotSuccessException {
		Request request = makeRequest(uri, accepts);
		request.setMethod(method);
		if (representation != null) {
			request.setEntity(representation);
		}
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		if (checkSuccess && !response.getStatus().isSuccess()) {
			logger.warn("Not success: " + response.getStatus());
			throw new NotSuccessException(response.getStatus());
		}
		return response;
	}

	private Response request(Method method, Reference uri, String data,
		MediaType mediaType, MediaType accepts) throws NotSuccessException {
		return request(method, uri, new StringRepresentation(data, mediaType),
			accepts, true);
	}

	private Request makeRequest(Reference uri, MediaType accepts) {
		Request request = new Request();
		request.setResourceRef(uri);
		if (accepts != null) {
			request.getClientInfo().getAcceptedMediaTypes().add(
				new Preference<MediaType>(accepts));
		}
		if (baseURI.isParent(uri)) {
			logger.debug("Authenticating as " + username);
			ChallengeResponse challengeResponse =
				new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username,
					password);
			request.setChallengeResponse(challengeResponse);
		} else {
			logger.warn("Not supplying credentials for out-of-site URI " + uri);
		}
		return request;
	}

	public Reference getCurrentUserURI() {
		checkCapabilities();
		return currentUserURI;
	}

	public Reference getQueuesURI() {
		checkCapabilities();
		return queuesURI;
	}

	public String getUsername() {
		return username;
	}

	public Reference getUsersURI() {
		checkCapabilities();
		return usersURI;
	}

	public Reference getWorkersURI() {
		checkCapabilities();
		return workersURI;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get a displayable name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the displayable name. This name is typically shown in UI components,
	 * and used by {@link #toString()}. If the name is null (the default),
	 * {@link #toString()} will return the {@link #baseURI} instead.
	 * 
	 * @see #getName()
	 * @see #toString()
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * A string representation of the context, if a name has been set with
	 * {@link #setName(String)}, that name will be returned, otherwise 
	 * {@link #getBaseURI()} will be used.
	 */
	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return getBaseURI().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof RESTContext)) {
			return false;
		}
		RESTContext other = (RESTContext) obj;
		if (! other.getBaseURI().equals(getBaseURI())) {
			return false;
		}
		if (! other.getUsername().equals(getUsername())) {
			return false;
		}
		// NOTE: Does not care about password or name
		return true;
	}
	
	@Override
	public int hashCode() {
		return (getBaseURI().toString() + getUsername()).hashCode();
	}
	

	

}
