package net.sf.taverna.t2.platform.util.download;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * The download manager is a central controller providing URL -> File mapping
 * with robust download management and verification including MD5 sum
 * verification where available. It is used by the plug-in manager and by the
 * various parts of raven.
 * 
 * @author Tom Oinn
 */
public interface DownloadManager {

	/**
	 * Asynchronous download method used to fetch a remote or cached URL.
	 * 
	 * @param sources
	 *            a list of one or more remote locations for the same file,
	 *            these will be used in order and treated as mirrors
	 * @param verifier
	 *            a DownloadVerifier used to check a completed download prior to
	 *            moving it to its final location
	 * @param mapper
	 *            a URLMapper used to map the supplied source URLs to a file
	 *            location for the downloaded file
	 * @param priority
	 *            an integer representing the priority of this job, higher
	 *            values denote higher priorities
	 * @param callback
	 *            a callback used to message success and failure to the caller
	 *            in an asynchronous fashion
	 */
	public void getAsFileAsynch(List<URL> sources, DownloadVerifier verifier,
			URLMapper mapper, int priority, DownloadCallback callback);

	/**
	 * Synchronous form of the download method, uses the asynch method
	 * internally and blocks, mostly provided as a convenience for tests - in
	 * general you should use the asynchronous form
	 * 
	 * @param sources
	 *            a list of one or more remote locations for the same file,
	 *            these will be used in order and treated as mirrors
	 * @param verifier
	 *            a DownloadVerifier used to check a completed download prior to
	 *            moving it to its final location
	 * @param mapper
	 *            a URLMapper used to map the supplied source URLs to a file
	 *            location for the downloaded file
	 * @param priority
	 *            an integer representing the priority of this job, higher
	 *            values denote higher priorities
	 * @return the downloaded File object
	 * @throws DownloadException
	 *             if a problem occurs during download
	 */
	public File getAsFile(List<URL> sources, DownloadVerifier verifier,
			URLMapper mapper, int priority) throws DownloadException;

}
