package net.sf.taverna.t2.platform.util.download.impl;

import net.sf.taverna.t2.platform.util.download.DownloadException;

/**
 * Thrown if an attempt is made to construct an invalid download job. This is
 * likely to happen in cases where multiple source URLs are specified and the
 * mapper provided maps these to different target file locations - this is not
 * allowed.
 * 
 * @author Tom Oinn
 * 
 */
public class DownloadJobCreationException extends DownloadException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7021896903657674322L;

	/**
	 * @param message
	 * @param cause
	 */
	public DownloadJobCreationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public DownloadJobCreationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public DownloadJobCreationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public DownloadJobCreationException() {
		// TODO Auto-generated constructor stub
	}

}
