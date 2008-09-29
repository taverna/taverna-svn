package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.service.rest.resources.representation.AbstractRepresentation;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.rest.utils.XmlBeansRepresentation;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * A resource that has several lazy-loaded representations. Representations are
 * implemented by subclassing one of the {@link AbstractRepresentation} classes
 * and adding them with
 * {@link #addRepresentation(net.sf.taverna.service.rest.resources.RepresentationalResource.AbstractRepresentation)}
 * in the constructor of the {@link RepresentationalResource} subclass.
 * <p>
 * Typically a subclass of {@link RepresentationalResource} can implement their
 * representations as nested classes so that they can access shared methods and
 * fields of the resource.
 * 
 * @see AbstractRepresentation
 * @see AbstractText
 * @see AbstractXML
 * @author Stian Soiland
 */
public class RepresentationalResource extends Resource {

	private static Logger logger =
		Logger.getLogger(RepresentationalResource.class);

	private URIFactory uriFactory;
	
	/**
	 * Map from {@link MediaType} to {@link AbstractRepresentation}, as
	 * registered with
	 * {@link #addRepresentation(net.sf.taverna.service.rest.resources.RepresentationalResource.AbstractRepresentation)}
	 */
	private Map<MediaType, AbstractRepresentation> representations =
		new HashMap<MediaType, AbstractRepresentation>();

	public RepresentationalResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		uriFactory = URIFactory.getInstance();
	}

	/**
	 * Register a representation by it's media type. Only one representation per
	 * media type is allowed.
	 * 
	 * @throws IllegalArgumentException
	 *             if the media type was already registered
	 * @param representation
	 *            {@link AbstractRepresentation} to register
	 */
	public void addRepresentation(AbstractRepresentation representation) {
		MediaType mediaType = representation.getMediaType();
		if (representations.containsKey(mediaType)) {
			throw new IllegalArgumentException("Already registered media type "
				+ mediaType);
		}
		getVariants().add(new Variant(mediaType));
		representations.put(mediaType, representation);
	}

	/**
	 * Retrieve the representation from abstract representations added with
	 * {@link #addRepresentation(net.sf.taverna.service.rest.resources.RepresentationalResource.AbstractRepresentation)}.
	 */
	@Override
	public Representation getRepresentation(Variant variant) {
		AbstractRepresentation representation =
			representations.get(variant.getMediaType());
		logger.info("Requested Media Type = "+variant.getMediaType());
		if (representation == null) {
			for (Entry<MediaType, AbstractRepresentation> entry : representations.entrySet()) {
				if (entry.getKey().includes(variant.getMediaType())) {
					representation = entry.getValue();
					logger.debug("Selected " + representation + " for " + variant.getMediaType());
					break;
				}
			}
		}
		if (representation == null) {
			logger.warn("No representation found for " + variant.getMediaType());
			return null;
		}
		return representation.getRepresentation(getRequest(),getResponse());
	}
	
	/**
	 * An abstract XMLBeans based representation.
	 */
	abstract class AbstractXML extends AbstractRepresentation {

		/**
		 * Return an XMLBeans Document that represents this resource. The
		 * document will be serialized by {@link #getRepresentation()}.
		 * 
		 * @return
		 */
		public abstract XmlObject getXML();

		@Override
		public MediaType getMediaType() {
			return MediaType.TEXT_XML;
		}

		@Override
		public Representation getRepresentation(Request request,Response response) {
			return new XmlBeansRepresentation(getXML(), getMediaType(), uriFactory);
		}
	}
}
