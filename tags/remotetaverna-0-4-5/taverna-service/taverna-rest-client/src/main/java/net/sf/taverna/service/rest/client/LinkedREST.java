package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Linked;

import org.restlet.data.Reference;

public class LinkedREST<LinkedClass extends Linked> extends AbstractREST<LinkedClass> {
	
	//private static Logger logger = Logger.getLogger(LinkedREST.class);
	
	public LinkedREST(RESTContext context, LinkedClass document) {
		super(context, context.makeReference(document.getHref()), document);
	}	

	public LinkedREST(RESTContext context, Reference uri, LinkedClass document) {
		super(context, uri, document);
	}
	
	public LinkedREST(RESTContext context, Reference uri,
		Class<LinkedClass> documentClass) {
		super(context, uri, documentClass);
	}
	
	@Override
	public synchronized LinkedClass getDocument() {
		LinkedClass document = super.getDocument();
		if (document.getHref() != null) {
			// Follow href instead, our document is probably
			// incomplete
			loadDocument();
			document = super.getDocument();
		}
		return document;
	}
	
	/**
	 * Get current document, which might be incomplete. Use
	 * {@link #getDocument()} if you want to be sure to get the full document.
	 * 
	 * @return The current (possibly incomplete) document if it has been
	 *         loaded, otherwise <code>null</code>
	 */
	public LinkedClass getCurrentDocument() {
		return super.getDocument();
	}
	
	
}
