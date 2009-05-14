package net.sf.taverna.t2.platform.util.download;

/**
 * Thrown when the download manager fails to honour a download request for
 * whatever reason.
 * 
 * @author Tom Oinn
 * 
 */
public class DownloadException extends RuntimeException {

	private static final long serialVersionUID = 3293889430892080382L;

	/**
	 * 
	 */
	public DownloadException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public DownloadException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DownloadException(Throwable cause) {
		super(cause);
	}

}
