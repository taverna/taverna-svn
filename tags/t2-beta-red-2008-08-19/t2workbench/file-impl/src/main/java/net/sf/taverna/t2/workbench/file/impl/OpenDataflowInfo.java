package net.sf.taverna.t2.workbench.file.impl;

import java.util.Date;

import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileType;

/**
 * Information about an open dataflow.
 * <p>
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class OpenDataflowInfo {

	private DataflowInfo dataflowInfo;
	private boolean isChanged;
	private Date openedAt;

	public OpenDataflowInfo() {
	}

	public FileType getFileType() {
		if (dataflowInfo == null) {
			return null;
		}
		return dataflowInfo.getFileType();
	}

	public Date getLastModified() {
		if (dataflowInfo == null) {
			return null;
		}
		return dataflowInfo.getLastModified();
	}

	public Date getOpenedAtDate() {
		return openedAt;
	}

	public Object getSource() {
		if (dataflowInfo == null) {
			return null;
		}
		return dataflowInfo.getCanonicalSource();
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setIsChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public synchronized void setOpenedFrom(DataflowInfo dataflowInfo) {
		setDataflowInfo(dataflowInfo);
		setOpenedAt(new Date());
		setIsChanged(false);
	}

	public synchronized void setSavedTo(DataflowInfo dataflowInfo) {
		setDataflowInfo(dataflowInfo);
		setIsChanged(false);
	}

	private void setDataflowInfo(DataflowInfo dataflowInfo) {
		this.dataflowInfo = dataflowInfo;
	}

	private void setOpenedAt(Date openedAt) {
		this.openedAt = openedAt;
	}

	public DataflowInfo getDataflowInfo() {
		return dataflowInfo;
	}
}