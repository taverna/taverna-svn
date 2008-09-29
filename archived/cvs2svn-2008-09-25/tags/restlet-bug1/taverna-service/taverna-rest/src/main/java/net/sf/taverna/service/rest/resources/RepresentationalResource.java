package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.service.rest.utils.XmlBeansRepresentation;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
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
		if (representation == null) {
			for (Entry<MediaType, AbstractRepresentation> entry : representations.entrySet()) {
				if (entry.getKey().includes(variant.getMediaType())) {
					representation = entry.getValue();
					break;
				}
			}
		}
		if (representation == null) {
			logger.warn("No representation found for " + variant.getMediaType());
			return null;
		}
		return representation.getRepresentation();
	}

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
	abstract class AbstractRepresentation {
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
		 * @return An inialized {@link Representation}
		 */
		public abstract Representation getRepresentation();
	}

	/**
	 * A plain text representation. By default, the language is not given, and
	 * the character set is {@link CharacterSet#ISO_8859_1}.
	 */
	abstract class AbstractText extends AbstractRepresentation {
		@Override
		public Representation getRepresentation() {
			return new StringRepresentation(getText(), getMediaType(),
				getLanguage(), getCharacterSet());
		}

		/**
		 * The {@link Language} of {@link #getText()}. By default
		 * <code>null</code>, which means language is unspecified.
		 * 
		 * @return The {@link Language} of the txt or <code>null</code>
		 */
		public Language getLanguage() {
			return null;
		}

		/**
		 * The character set to be used for serializing the text. The default
		 * for <code>text/*</code> media types is
		 * {@link CharacterSet#ISO_8859_1}, otherwise
		 * {@link CharacterSet#UTF_8}.
		 * 
		 * @return
		 */
		public CharacterSet getCharacterSet() {
			if (MediaType.TEXT_ALL.includes(getMediaType())) {
				return CharacterSet.ISO_8859_1;
			}
			return CharacterSet.UTF_8;
		}

		/**
		 * Return a plain text representation. There is no real restrictions on
		 * the plain text except that it should be both human-readable and
		 * easily parsable. A RFC-822 style is often convenient, for example:
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
		public abstract String getText();

		/**
		 * By default, the media type is <code>text/plain</code>
		 */
		@Override
		public MediaType getMediaType() {
			return MediaType.TEXT_PLAIN;
		}
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
		public Representation getRepresentation() {
			return new XmlBeansRepresentation(getXML(), getMediaType());
		}
	}

}
