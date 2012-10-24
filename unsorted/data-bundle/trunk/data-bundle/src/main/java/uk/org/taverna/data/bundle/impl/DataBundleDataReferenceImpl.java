/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataReference;

/**
 * @author alanrw
 *
 */
public class DataBundleDataReferenceImpl implements DataReference {

	private DataBundleImpl containingBundle;

	private static Logger logger = Logger.getLogger(DataBundleDataImpl.class);

	private String id;
	
	public DataBundleDataReferenceImpl(DataBundleImpl containingBundle, String id) {
		this.containingBundle = containingBundle;
		this.id = id;
	}

	static String getBasePath(String id) {
		try {
			id = UUID.fromString(id).toString();
		} catch (IllegalArgumentException ex) {
			logger.error("Unable to convert id", ex);
		}
		return "DataReferences/" + id.substring(
				0, 2) + "/" +  id;
	}
	
	static String getBasePath(DataReference dr) {
		String id = dr.getID();
		return getBasePath(id);
	}

	private String getBasePath() {
		return getBasePath(this);
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.DataReference#getURI()
	 */
	@Override
	public URI getURI() {
		try {
			return new URI(containingBundle.getResourceAsString(getDataReferenceURIResourcePath()));
		} catch (URISyntaxException e) {
			logger.error(e);
			return null;
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.DataReference#getCharset()
	 */
	@Override
	public Charset getCharset() {
		try {
			return Charset.forName(containingBundle.getResourceAsString(getDataReferenceCharsetResourcePath()));
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	public void setURI(URI uri) throws IOException {
		containingBundle.addResource(uri.toASCIIString(), getDataReferenceURIResourcePath(), "text/uri");
	}

	public String getDataReferenceResourcePath() {
		return getBasePath();
	}

	private String getDataReferenceURIResourcePath() {
		return getDataReferenceResourcePath() + "/URI";
	}

	private String getDataReferenceCharsetResourcePath() {
		return getDataReferenceResourcePath() + "/Charset";
	}

	public void setCharset(Charset charset) throws IOException {
		containingBundle.addResource(charset.name(), getDataReferenceCharsetResourcePath(), "text/plain");
	}

	@Override
	public String getID() {
		return id;
	}

}
