package net.sf.taverna.t2.workbench.file;

import java.util.Date;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class DataflowInfo {
	private final FileType fileType;
	private final Dataflow dataflow;
	private final Date lastModified;
	private final Object canonicalSource;

	public DataflowInfo(FileType fileType, Object canonicalSource,
			Dataflow dataflow, Date lastModified) {
		this.fileType = fileType;
		this.canonicalSource = canonicalSource;
		this.dataflow = dataflow;
		this.lastModified = lastModified;
	}

	public DataflowInfo(FileType fileType, Object canonicalSource,
			Dataflow dataflow) {
		this(fileType, canonicalSource, dataflow, null);
	}

	public Object getCanonicalSource() {
		return canonicalSource;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public FileType getFileType() {
		return fileType;
	}
}
