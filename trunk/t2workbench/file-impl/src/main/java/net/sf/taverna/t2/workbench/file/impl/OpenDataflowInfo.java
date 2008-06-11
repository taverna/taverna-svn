/**
 * 
 */
package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

public class OpenDataflowInfo {

	private boolean isChanged;
	private long lastModified = 0;
	private Date openedAt;
	private URL url;

	public OpenDataflowInfo() {
	}

	public File getFile() {
		URL url = getURL();
		if (url == null) {
			return null;
		}
		if (!url.getProtocol().equals("file")) {
			return null;
		}
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			FileManagerImpl.logger.warn("Invalid URI " + url, e);
			return null;
		}
	}

	public long getLastModified() {
		return lastModified;
	}

	public synchronized Date getLastModifiedAtDate() {
		lastModified = getLastModified();
		if (lastModified == 0) {
			return null;
		}
		return new Date(lastModified);
	}

	public Date getOpenedAtDate() {
		return openedAt;
	}

	public URL getURL() {
		return url;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public synchronized void setOpenedFrom(URL url) {
		this.url = url;
		openedAt = new Date();
		updateLastModified();
	}

	public synchronized void setSavedTo(File dataflowFile) {
		try {
			dataflowFile = dataflowFile.getCanonicalFile();
		} catch (IOException e) {
			FileManagerImpl.logger.warn("Could not resolve path for "
					+ dataflowFile);
		}
		try {
			url = dataflowFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not create URL from "
					+ dataflowFile);
		}
		updateLastModified();
		setChanged(false);
	}

	protected synchronized void updateLastModified() {
		File file = getFile();
		if (file != null) {
			lastModified = file.lastModified();
		} else {
			lastModified = 0;
		}
	}
}