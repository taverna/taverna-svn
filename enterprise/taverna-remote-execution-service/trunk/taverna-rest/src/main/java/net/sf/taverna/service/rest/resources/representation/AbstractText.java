package net.sf.taverna.service.rest.resources.representation;


import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

public /**
 * A plain text representation. By default, the language is not given, and
 * the character set is {@link CharacterSet#ISO_8859_1}.
 */
abstract class AbstractText extends AbstractRepresentation {
	@Override
	public Representation getRepresentation(Request request,Response response) {
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