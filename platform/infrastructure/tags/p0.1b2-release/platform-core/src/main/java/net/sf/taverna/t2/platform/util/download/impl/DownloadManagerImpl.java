package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import net.sf.taverna.t2.platform.util.download.DownloadCallback;
import net.sf.taverna.t2.platform.util.download.DownloadException;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;
import net.sf.taverna.t2.platform.util.download.URLMapper;

public class DownloadManagerImpl implements DownloadManager {

	private BlockingQueue<DownloadJob> pendingJobs = new PriorityBlockingQueue<DownloadJob>();
	private List<DownloadJob> activeJobs = new ArrayList<DownloadJob>();
	private boolean printDownloads = false;

	public void setPrintDownloads(String value) {
		this.printDownloads = Boolean.parseBoolean(value);
	}

	/**
	 * Create a simple download manager instance with the specified number of
	 * worker threads
	 */
	public DownloadManagerImpl(int workers) {
		for (int i = 0; i < workers; i++) {
			Thread worker = new Thread(null, new Runnable() {
				public void run() {
					while (true) {
						DownloadJob job;
						try {
							job = pendingJobs.take();
							activateJob(job);
							job.enact();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}, "Download worker " + i);
			worker.setDaemon(true);
			worker.start();
		}
	}

	/**
	 * Complete a job, removing it from the active set
	 * 
	 * @param job
	 */
	synchronized void completeJob(DownloadJob job) {
		activeJobs.remove(job);
		if (printDownloads) {
			System.out.println(job.getLastURL());
		}
	}

	/**
	 * Create a new download job, placing it on the queue
	 * 
	 * @param job
	 */
	synchronized void createJob(DownloadJob job) {
		pendingJobs.add(job);
	}

	/**
	 * Activate a job, pulling it from the queue into the active set. This
	 * method blocks on the queue if no elements are present.
	 */
	synchronized DownloadJob activateJob(DownloadJob job) {
		activeJobs.add(job);
		return job;
	}

	public synchronized void getAsFileAsynch(List<URL> sources,
			DownloadVerifier verifier, URLMapper mapper, int priority,
			DownloadCallback callback) {
		// First determine whether we already downloaded the file, in which case
		// we can just return it immediately
		if (sources.isEmpty()) {
			throw new DownloadException("No download sources specified!");
		}
		File targetLocation = mapper.map(sources.get(0));
		if (targetLocation.exists()) {
			callback.downloadCompleted(targetLocation);
			return;
		}

		// Next determine whether we already have a job with this target
		// location anywhere in the system, in which case we just add the
		// callback to that job and return.
		for (DownloadJob job : activeJobs) {
			try {
				if (job.getTargetLocation().getCanonicalPath().equals(
						targetLocation.getCanonicalPath())) {
					job.addCallback(callback);
					return;
				}
			} catch (IOException e) {
				// Should never happen!
				throw new DownloadException(e);
			}
		}
		for (DownloadJob job : pendingJobs) {
			try {
				if (job.getTargetLocation().getCanonicalPath().equals(
						targetLocation.getCanonicalPath())) {
					job.addCallback(callback);
					return;
				}
			} catch (IOException e) {
				// Should never happen!
				throw new DownloadException(e);
			}
		}

		// If we've got here we definitely don't have the file, neither do we
		// have a corresponding active or pending job in the download manager's
		// system. This means we need a new job in the queue to represent this
		// download task.
		try {
			DownloadJob newJob = new DownloadJob(sources, mapper, verifier,
					priority, this);
			newJob.addCallback(callback);
			createJob(newJob);
		} catch (DownloadJobCreationException e) {
			callback.downloadFailed(e);
		}
	}

	public File getAsFile(List<URL> sources, DownloadVerifier verifier,
			URLMapper mapper, int priority) throws DownloadException {
		SynchronousCallbackHelper callback = new SynchronousCallbackHelper(
				Thread.currentThread());
		if (sources.isEmpty()) {
			throw new DownloadException("No download sources specified!");
		}
		// System.out.println("Created callback");
		getAsFileAsynch(sources, verifier, mapper, priority, callback);
		// System.out.println("Called asynch method");
		if (callback.completed) {
			// System.out.println("Callback completed first time");
			if (callback.result != null) {
				// System.out.println("Returning file");
				return callback.result;
			}
			throw callback.fault;
		}
		while (true) {
			try {
				// System.out.println("Sleeping");
				Thread.sleep(1000);
				if (callback.completed) {
					if (callback.result != null) {
						return callback.result;
					}
					throw callback.fault;
				}
			} catch (InterruptedException ie) {
				// System.out.println("Interrupted!");
				if (callback.completed) {
					if (callback.result != null) {
						return callback.result;
					}
					throw callback.fault;
				}
			}
		}
	}

	class SynchronousCallbackHelper implements DownloadCallback {

		boolean completed = false;
		File result = null;
		DownloadException fault = null;
		Thread parentThread;

		SynchronousCallbackHelper(Thread t) {
			this.parentThread = t;
		}

		public void downloadCompleted(File result) {
			synchronized (DownloadManagerImpl.this) {
				this.result = result;
				this.completed = true;
				parentThread.interrupt();
			}
		}

		public void downloadFailed(DownloadException exception) {
			synchronized (DownloadManagerImpl.this) {
				this.fault = exception;
				this.completed = true;
				parentThread.interrupt();
			}
		}

	}
}
