package net.sf.taverna.service.rest.client;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

public class AbstractREST<DocumentClass extends XmlObject> {

	private static Logger logger = Logger.getLogger(AbstractREST.class);

	RESTContext context;

	private String uri;

	private DocumentClass document;

	private Class<? extends DocumentClass> documentClass;

	public AbstractREST(RESTContext context, String uri,
		Class<DocumentClass> documentClass) {
		this(context, uri, null, documentClass);
	}

	public AbstractREST(RESTContext context, String uri, DocumentClass document) {
		this(context, uri, document, null);
	}

	private AbstractREST(RESTContext context, String uri,
		DocumentClass document, Class<DocumentClass> documentClass) {
		this.context = context;
		this.document = document;
		this.uri = uri;
		if (context == null) {
			throw new NullPointerException("RESTContext can't be null");
		}
		if (uri == null) {
			throw new NullPointerException("uri can't be null");
		}
		if (document == null && documentClass == null) {
			throw new NullPointerException(
				"At least one of DocumentClass or String can't be null");
		}
		this.documentClass = documentClass;
	}

	public synchronized DocumentClass getDocument() {
		if (document == null) {
			loadDocument();
		}
		return document;
	}

	public void setDocument(DocumentClass document) {
		this.document = document;
	}

	public void loadDocument() {
		setDocument(context.loadDocument(uri, getDocumentClass()));
	}

	@SuppressWarnings("unchecked")
	public Class<? extends DocumentClass> getDocumentClass() {
		if (document != null) {
			return (Class<? extends DocumentClass>) document.getClass();
		}
		return documentClass;
	}

	public String getURI() {
		return uri;
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
		return getDocumentClass() + " " + getURI();
	}

}