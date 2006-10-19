package net.sf.taverna.raven.repository.impl;

import net.sf.taverna.raven.repository.DownloadStatus;

/**
 * Container bean for download progress tracking
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
	
	void setReadBytes(int read) {
		this.read = read;
	}
	
	public int getTotalBytes() {
		return this.total;
	}

	public int getReadBytes() {
		return this.read;
	}

  public void setFinished()
  {
    finished = true;
  }

  public boolean isFinished()
  {
    return finished;
  }
}
