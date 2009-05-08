package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.util.download.DownloadCallback;
import net.sf.taverna.t2.platform.util.download.DownloadException;
import net.sf.taverna.t2.platform.util.download.DownloadVerificationException;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;
import net.sf.taverna.t2.platform.util.download.URLMapper;

/**
 * A single download request with a list of source URLs, a URLMapper defining
 * the target file and a DownloadVerifier. The verifier may be null. Each job
 * also has a priority rating used to order it in the download queue, this can
 * be used to prioritise short downloads such as pom files over longer ones such
 * as jars.
 * 
 * @author Tom Oinn
 * 
 */
public class DownloadJob implements Comparable<DownloadJob> {

	private int priority = 0;
	private DownloadVerifier verifier;
	private List<URL> sources;
	private List<DownloadCallback> callbacks = new ArrayList<DownloadCallback>();
	private File targetLocation;
	private boolean completed = false;
	private DownloadManagerImpl manager;
	private URL lastURL = null;

	/**
	 * Construct a new download job
	 * 
	 * @param sources
	 *            a list of URLs which can be treated in order as mirrors for
	 *            the same data
	 * @param mapper
	 *            a file mapper to determine where the file should be placed
	 *            after download and verification
	 * @param verifier
	 *            a download verifier, which may be null, to check a downloaded
	 *            file prior to storing it to the location defined by the url
	 *            mapper
	 * @param priority
	 *            a priority value for this download job, with higher values
	 *            corresponding to higher priority jobs. Value must be positive.
	 */
	public DownloadJob(List<URL> sources, URLMapper mapper,
			DownloadVerifier verifier, int priority, DownloadManagerImpl manager)
			throws DownloadJobCreationException {

		// Check and assign download manager
		if (manager == null) {
			throw new DownloadJobCreationException(
					"Download manager cannot be null");
		}
		this.manager = manager;

		// Check and assign source URL list
		if (sources == null) {
			throw new DownloadJobCreationException(
					"Source URL list cannot be null");
		}
		// Check that there is at least one source URL
		if (sources.size() == 0) {
			throw new DownloadJobCreationException(
					"Must specify at least one source URL for a download job");
		}
		this.sources = sources;

		// Check and assign url mapper
		if (mapper == null) {
			throw new DownloadJobCreationException("URL mapper cannot be null");
		}
		// Check that the supplied mapper maps all source URLs to the same file
		// location
		this.targetLocation = mapper.map(sources.get(0));
		for (URL sourceURL : sources) {
			File perSourceTarget = mapper.map(sourceURL);
			try {
				if (perSourceTarget.getCanonicalPath().equals(
						this.targetLocation.getCanonicalPath()) == false) {
					throw new DownloadJobCreationException(
							"All source URLs must map to the same target under the specified URL mapper");
				}
			} catch (IOException e) {
				throw new DownloadJobCreationException(
						"Exception when checking URL mapping for '"+perSourceTarget+"'", e);
			}
		}

		// Use the specified verifier, or, if null, one that always passes
		if (verifier != null) {
			this.verifier = verifier;
		} else {
			this.verifier = new DownloadVerifier() {
				public boolean verify(File downloadedFile, URL sourceLocation)
						throws DownloadVerificationException {
					return true;
				}
			};
		}

		if (priority < 0) {
			throw new DownloadJobCreationException(
					"Priority cannot be negative");
		}
		this.priority = priority;

	}

	/**
	 * Add a download callback to this job
	 */
	public void addCallback(DownloadCallback callback) {
		synchronized (this.manager) {
			if (completed) {
				throw new DownloadException(
						"Unable to register a callback on a completed download job");
			}
			this.callbacks.add(callback);
		}
	}

	/**
	 * Return the target file location for this download job
	 */
	public File getTargetLocation() {
		return this.targetLocation;
	}

	/**
	 * Return the priority of this job, high values indicate jobs with a higher
	 * priority, default is zero. Used by the job queue to determine which job
	 * to call next
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Natural order based on priority setting, will be positive if the priority
	 * of this job is higher than that of the target
	 */
	public int compareTo(DownloadJob other) {
		return this.priority - other.getPriority();
	}

	/**
	 * Run the download job synchronously, notifying any listeners of completion
	 * or failure. Returns true if the download completed, false otherwise.
	 */
	boolean enact() {
		DownloadException latestException = new DownloadException(
				"Unspecified download error");
		for (URL sourceURL : sources) {
			// Magic processing for classpath URLs!
			if (sourceURL.getHost().equalsIgnoreCase("classpath")) {
				String classPathResource = sourceURL.getPath();
				// Chomp off leading '/' character as it seems to cause issues
				if (classPathResource.startsWith("/")) {
					classPathResource = classPathResource.substring(1);
				}
				sourceURL = Thread.currentThread().getContextClassLoader()
						.getResource(classPathResource);
				if (sourceURL == null) {
					latestException = new DownloadException(
							"Unable to find classpath resource '"
									+ classPathResource + "'");
					continue;
				}
			}

			File tempFile = null;
			try {
				lastURL = sourceURL;
				// System.out.println(sourceURL);
				// tempFile = File.createTempFile("t2dlm", "tmp");
				tempFile = File.createTempFile("t2dlm", "tmp",
						this.targetLocation.getParentFile());
				InputStream is = sourceURL.openStream();
				FileOutputStream fos = new FileOutputStream(tempFile);
				copyStream(is, fos);
				is.close();
				fos.flush();
				fos.close();
				// Attempt to verify the file, this throws a verification
				// exception on failure
				verifier.verify(tempFile, sourceURL);
				// Rename the file to the target
				if (tempFile.renameTo(this.targetLocation)) {
					notifySuccess();
					return true;
				} else {
					throw new DownloadException("Unable to move file!");
				}
			} catch (Exception e) {
				if (e instanceof DownloadException) {
					latestException = (DownloadException) e;
				} else {
					latestException = new DownloadException(e);
				}
				if (tempFile != null) {
					tempFile.delete();
				}
			}
		}
		// Got to here so the job failed
		notifyFailure(latestException);
		return false;
	}

	URL getLastURL() {
		return this.lastURL;
	}

	void copyStream(InputStream is, OutputStream os) throws IOException {
		int totalbytes = 0;
		byte[] buffer = new byte[1024];
		int bytesRead;
		try {
			while ((bytesRead = is.read(buffer)) != -1) {
				totalbytes += bytesRead;
				os.write(buffer, 0, bytesRead);
			}
		} finally {
			os.flush();
			os.close();
		}
	}

	private void notifySuccess() {
		synchronized (this.manager) {
			if (completed) {
				throw new DownloadException(
						"Can't notify more than once for any given download job");
			}
			for (DownloadCallback callback : callbacks) {
				callback.downloadCompleted(this.targetLocation);
			}
			completed = true;
			this.manager.completeJob(this);
		}
	}

	private void notifyFailure(DownloadException e) {
		synchronized (this.manager) {
			if (completed) {
				throw new DownloadException(
						"Can't notify more than once for any given download job");
			}
			for (DownloadCallback callback : callbacks) {
				callback.downloadFailed(e);
			}
			completed = true;
			this.manager.completeJob(this);
		}
	}

}
