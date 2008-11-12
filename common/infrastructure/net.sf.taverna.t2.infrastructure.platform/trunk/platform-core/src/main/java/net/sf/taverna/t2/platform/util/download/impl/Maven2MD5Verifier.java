package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.net.URL;

import net.sf.taverna.t2.platform.util.download.DownloadVerificationException;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;

/**
 * Implementation of DownloadVerifier which uses a remotely accessed MD5 sum to
 * verify the downloaded file. This particular verifier assumes the structure
 * used by maven 2 repositories, that is to say the MD5 sum for foo.xml is
 * located at foo.xml.md5 in the same path.
 * 
 * @author Tom Oinn
 * 
 */
public class Maven2MD5Verifier implements DownloadVerifier {

	public boolean verify(File downloadedFile, URL sourceLocation)
			throws DownloadVerificationException {
		String remoteMD5hash, localMD5hash;
		try {
			URL remoteMD5 = new URL(sourceLocation.toExternalForm() + ".md5");
			remoteMD5hash = MD5Support.getURLContentsAsHexString(remoteMD5).substring(0,31);
			localMD5hash = MD5Support
					.getMD5FileHashAsAlphaString(downloadedFile).substring(0,31);
		} catch (Exception e) {
			return false;
		}
		if (remoteMD5hash.equals(localMD5hash)) {
			System.out.println("MD5 hash verified");
			return true;
		} else {
			throw new DownloadVerificationException(
					"MD5 signatures found but do not match, remote='"
							+ remoteMD5hash + "' local='" + localMD5hash + "'");
		}
	}

}
