package net.sf.taverna.raven.repository.impl;

import net.sf.taverna.raven.repository.DownloadStatus;

/**
 * Container bean for download progress tracking
 * 
 * @author Tom
 */
public class DownloadStatusImpl implements DownloadStatus {

	private int total;
	private int read;
	private boolean finished;

	DownloadStatusImpl(int total) {
		this.total = total;
		this.read = 0;
		this.finished = false;
	}

	public int getReadBytes() {
		return this.read;
	}

	public int getTotalBytes() {
		return this.total;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished() {
		finished = true;
	}

	void setReadBytes(int read) {
		this.read = read;
	}
}
