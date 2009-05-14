package net.sf.taverna.t2.platform.util.download;

import java.io.File;

/**
 * Used by asynchronous methods in the download manager. Note - callbacks are
 * made in threads which are shared within the download manager, so do not do
 * any significant work in the callback thread itself!
 * 
 * @author Tom Oinn
 * 
 */
public interface DownloadCallback {

	/**
	 * Called when a file has successfuly been downloaded
	 * 
	 * @param result
	 *            the File containing the downloaded data
	 */
	public void downloadCompleted(File result);

	/**
	 * Called when a download has failed
	 * 
	 * @param exception
	 *            the failure message
	 */
	public void downloadFailed(DownloadException exception);

}
