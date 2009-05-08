package net.sf.taverna.t2.platform.util.download;

import java.io.File;
import java.net.URL;

/**
 * Maps URLs to downloaded file locations. This allows calling code to implement
 * different strategies for file naming in the download manager. The download
 * manager uses the supplied implementation of the URLMapper to determine the
 * name of the file to create when a download completes, and also to determine
 * whether a download job for that file is already in progress. This means that
 * download jobs are hashed on the result of the URLMapper call, not on their
 * original source.
 * <p>
 * This is also the interface to use to create distinct partitions within the
 * download manager, such as one for poms, one for jar files and one for jars
 * from artifacts.
 * 
 * @author Tom Oinn
 * 
 */
public interface URLMapper {

	/**
	 * Given a source URL return the local File to which that URL will be
	 * downloaded
	 * 
	 * @param source
	 *            the download URL
	 * @return the file location to be used by the download manager for a
	 *         completed download
	 */
	public File map(URL source);

}
