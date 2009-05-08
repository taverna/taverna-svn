package net.sf.taverna.t2.platform.util.download;

import java.io.File;
import java.net.URL;

/**
 * Used to check the integrity of a completed download, implementations would
 * include one to check the MD5 sum of downloaded pom and jar files from maven
 * repositories
 * 
 * @author Tom Oinn
 * 
 */
public interface DownloadVerifier {

	/**
	 * Called to verify a downloaded file. The verifier should return true if
	 * any verification was carried out and passed, false if verification could
	 * not be carried out (i.e. an MD5 sum based verifier without access to the
	 * MD5 sum) and throw an exception if verification was performed but failed.
	 * 
	 * @param downloadedFile
	 *            the (probably temporary) file on the local storage system
	 *            which the verifier is trying to verify.
	 * @param sourceLocation
	 *            the original URL from which the file was downloaded, used to
	 *            assemble relative paths to e.g. MD5 hash
	 * @return whether verification was performed, false indicates that
	 *         preconditions for verification weren't satisfied and translates
	 *         roughly to 'don't know' in terms of file validity
	 * @throws DownloadVerificationException
	 *             if verification was performed and the file failed to verify
	 *             correctly
	 */
	public boolean verify(File downloadedFile, URL sourceLocation)
			throws DownloadVerificationException;

}
