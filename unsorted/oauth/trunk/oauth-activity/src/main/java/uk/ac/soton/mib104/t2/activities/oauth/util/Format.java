package uk.ac.soton.mib104.t2.activities.oauth.util;

/**
 * Format is an enumeration.
 * 
 * A Format object encapsulates the state information of a content format. 
 * This state information includes:
 * <ul>
 * <li>The Internet media type (MIME type).</li>
 * </ul>
 * 
 * @author Mark Borkum
 */
public enum Format {

	APPLICATION_JSON("application/json"), 
	TEXT_PLAIN("text/plain"),
	;
	
	private final String mimeType;
	
	/**
	 * Sole constructor.
	 * 
	 * @param mimeType  the Internet media type (MIME type).
	 */
	Format(final String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Returns the Internet media type (MIME type).
	 * 
	 * @return  the Internet media type (MIME type).
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	@Override
	public String toString() {
		return this.getMimeType();
	}
	
}
