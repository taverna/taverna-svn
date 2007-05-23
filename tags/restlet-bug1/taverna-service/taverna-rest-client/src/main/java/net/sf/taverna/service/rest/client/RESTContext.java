package net.sf.taverna.service.rest.client;

import java.io.IOException;

import net.sf.taverna.service.interfaces.TavernaService;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class RESTContext {

	public static final MediaType restType =
		new MediaType(TavernaService.restType);

	public static final MediaType scuflType =
		new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType =
		new MediaType(TavernaService.baclavaType);

	private static Client client = new Client(Protocol.HTTP);

	private static Logger logger = Logger.getLogger(RESTContext.class);

	private Reference baseURI;

	private String username;

	private String password;

	private UserREST user;

	/**
	 * Register a dummy user
	 * 
	 * @param baseURI
	 * @return
	 * @throws NotSuccessException
	 */
	public static RESTContext register(String baseURI)
		throws NotSuccessException {
		Request request = new Request();

		// FIXME: Use capabilities listing to get URI
		request.setResourceRef(baseURI + "users");
		request.setMethod(Method.POST);
		request.setEntity("<user xmlns='" + TavernaService.NS + "'>"
			+ "</user>", restType);
		Response response = client.handle(request);
		if (!response.getStatus().isSuccess()) {
			throw new NotSuccessException(response.getStatus());
		}
		Reference justCreated = response.getRedirectRef();
		String username = justCreated.getLastSegment();
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

	public RESTContext(String baseURI, String username, String password) {
		this.baseURI = new Reference(baseURI);
		// this.challengeResponse =
		// new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
		// username, password);
		this.username = username;
		this.password = password;
		// findCapabilities();

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

	public UserREST getUser() {
		if (user == null) {
			// FIXME: Hard-coded URI-schema "users"
			Reference userURL = new Reference(baseURI, "users/" + username);
			user = new UserREST(this, userURL);
		}
		return user;
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

	private Response request(Method method, Reference uri, String data,
		MediaType mediaType, MediaType accepts) throws NotSuccessException {
		Request request = makeRequest(uri, accepts);
		request.setMethod(method);
		if (data != null) {
			request.setEntity(data, mediaType);
		}
		Response response = client.handle(request);
		if (!response.getStatus().isSuccess()) {
			logger.warn("Not success: " + response.getStatus());
			throw new NotSuccessException(response.getStatus());
		}
		return response;
	}

	public Response get(Reference uri) throws NotSuccessException, MediaTypeException {
		return get(uri, restType);
	}

	public Response get(Reference uri, MediaType accepts)
		throws NotSuccessException, MediaTypeException {
		Response response = request(Method.GET, uri, null, null, accepts);
		MediaType mediaType = response.getEntity().getMediaType();
		if (! accepts.includes(mediaType)) {
			throw new MediaTypeException(accepts, mediaType);
		}
		return response;
	}

	public Response post(Reference uri, String data, MediaType mediaType)
		throws NotSuccessException {
		return request(Method.POST, uri, data, mediaType, null);
	}

	public Response put(Reference uri, String data, MediaType mediaType)
		throws NotSuccessException {
		return request(Method.PUT, uri, data, mediaType, null);
	}

	public Response delete(Reference uri) throws NotSuccessException {
		return request(Method.DELETE, uri, null, null, null);
	}

	public Reference getBaseURI() {
		return baseURI;
	}

	public Reference makeReference(String uri) {
		return new Reference(getBaseURI(), uri);
	}

	public Response post(Reference uri, XmlObject document)
		throws NotSuccessException {
		// FIXME: Should stream the XML and use document.save()
		return post(uri, document.xmlText(), restType);
	}

}
