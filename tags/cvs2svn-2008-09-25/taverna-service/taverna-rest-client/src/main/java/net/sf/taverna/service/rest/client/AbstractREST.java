package net.sf.taverna.service.rest.client;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class AbstractREST<DocumentClass extends XmlObject> {

	private static Logger logger = Logger.getLogger(AbstractREST.class);

	RESTContext context;

	private Reference uriReference;

	private DocumentClass document;

	private Class<? extends DocumentClass> documentClass;

	private boolean invalid;

	public AbstractREST(RESTContext context, Reference uri,
		Class<DocumentClass> documentClass) {
		this(context, uri, null, documentClass);
	}

	public AbstractREST(RESTContext context, Reference uri,
		DocumentClass document) {
		this(context, uri, document, null);
	}

	private AbstractREST(RESTContext context, Reference uri,
		DocumentClass document, Class<DocumentClass> documentClass) {
		if (context == null) {
			throw new NullPointerException("RESTContext can't be null");
		}
		if (uri == null) {
			throw new NullPointerException("uriReference can't be null");
		}
		if (document == null && documentClass == null) {
			throw new NullPointerException(
				"At least one of DocumentClass or String can't be null");
		}
		this.context = context;
		this.document = document;
		this.uriReference = uri.getTargetRef();
		this.documentClass = documentClass;
	}

	public synchronized DocumentClass getDocument() {
		if (document == null || invalid) {
			loadDocument();
		}
		return document;
	}

	public void setDocument(DocumentClass document) {
		this.document = document;
	}
	
	/**
	 * Force the next {@link #getDocument()} to do a full load.
	 *
	 */
	public void invalidate() {
		invalid = true;
	}

	public void refresh() {
		loadDocument();
	}

	public void loadDocument() {
		setDocument(context.loadDocument(uriReference, getDocumentClass()));
		invalid = false;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends DocumentClass> getDocumentClass() {
		if (document != null) {
			return (Class<? extends DocumentClass>) document.getClass();
		}
		return documentClass;
	}

	public String getURI() {
		return getURIReference().toString();
	}

	public Reference getURIReference() {
		return uriReference;
	}

	public Reference getRelativeReference(String uri) {
		return new Reference(getURIReference(), uri).getTargetRef();
	}
	

	public String getString(Reference uriReference, MediaType mediaType) throws RESTException {
		Response response = context.get(uriReference, mediaType);
		try {
			return response.getEntity().getText();
		} catch (IOException e) {
			throw new RESTException("Could not receive  " + uriReference, e);
		}
	}
	
	public String getString(String uri, MediaType mediaType) throws RESTException {
		Reference uriReference = getRelativeReference(uri);
		return getString(uriReference, mediaType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractREST)) {
			return false;
		}
		AbstractREST other = (AbstractREST) obj;
		if (getURI() == null) {
			logger.warn(this + " does not have an URI");
			return false;
		}
		return getURI().equals(other.getURI());
	}

	@Override
	public int hashCode() {
		if (getURI() == null) {
			logger.warn(this + " does not have an URI");
			return super.hashCode();
		}
		return getURI().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getURI();
	}
	
}