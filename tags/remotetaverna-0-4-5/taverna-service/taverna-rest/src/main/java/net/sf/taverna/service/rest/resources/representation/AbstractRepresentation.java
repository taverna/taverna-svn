package net.sf.taverna.service.rest.resources.representation;

import net.sf.taverna.service.rest.resources.AbstractResource;

import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * An abstract representation that can be registered with
 * {@link AbstractResource#addRepresentation(net.sf.taverna.service.rest.resources.AbstractResource.AbstractRepresentation)}
 * <p>
 * Implementations must provide the {@link MediaType} and
 * {@link Representation}. Since {@link #getRepresentation()} will only be
 * called if this particular representation is served, this class provides
 * lazy representation, since say building an XML document can be done when
 * calling {@link #getRepresentation()}.
 * <p>
 * Note that an {@link AbstractRepresentation} is not a
 * {@link Representation}, but it's {@link #getRepresentation()} return the
 * "real" {@link Representation}.
 * 
 * @see AbstractText
 * @see AbstractXML
 */
public abstract class AbstractRepresentation {
	/**
	 * Provide the media type, for example {@link MediaType#TEXT_PLAIN}
	 * 
	 * @return A {@link MediaType}
	 */
	public abstract MediaType getMediaType();

	/**
	 * Generate if needed, and return this representation. The
	 * representation's media type should be included in
	 * {@link #getMediaType()}.
	 * 
	 * @param request the Request
	 * @param response the Response
	 * @return An initialized {@link Representation}
	 */
	
	public abstract Representation getRepresentation(Request request,Response response);
	
	@Override
	public String toString() {
		return getClass().getName() + " " + getMediaType().toString();
	}
}