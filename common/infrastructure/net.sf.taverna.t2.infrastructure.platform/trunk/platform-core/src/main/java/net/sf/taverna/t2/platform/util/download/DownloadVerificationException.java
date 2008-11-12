package net.sf.taverna.t2.platform.util.download;

/**
 * Subclass of DownloadException thrown by DownloadVerifier implementations
 * 
 * @author Tom Oinn
 * 
 */
public class DownloadVerificationException extends DownloadException {

	private static final long serialVersionUID = -4630647359812475969L;

	public DownloadVerificationException() {
		// TODO Auto-generated constructor stub
	}

	public DownloadVerificationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DownloadVerificationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public DownloadVerificationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
