package net.sf.taverna.raven.repository;

/**
 * The status of an active download
 * 
 * @author Tom Oinn
 */
public interface DownloadStatus {

	/**
	 * @return total number of bytes read from the download source
	 */
	public abstract int getReadBytes();

	/**
	 * @return total number of bytes to read from the download source, or -1 if
	 *         not known
	 */
	public abstract int getTotalBytes();

	/**
	 * 
	 * @return true if the download has finnished (successfully, or in error),
	 *         false otherwise
	 */
	public abstract boolean isFinished();
}
