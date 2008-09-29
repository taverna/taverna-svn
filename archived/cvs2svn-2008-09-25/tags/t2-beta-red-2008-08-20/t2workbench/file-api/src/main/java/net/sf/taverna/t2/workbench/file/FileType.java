package net.sf.taverna.t2.workbench.file;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A filetype to identify a way to (de)serialise a {@link Dataflow} with the
 * {@link FileManager}.
 * <p>
 * Two filetypes are considered equal if they share an extension or mime type or
 * are the same instance.
 * </p>
 * 
 * @see net.sf.taverna.t2.workbench.file.impl.T2FlowFileType
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class FileType {

	public abstract String getExtension();

	public abstract String getMimeType();

	public abstract String getDescription();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof FileType)) {
			return false;
		}
		FileType other = (FileType) obj;
		if (getMimeType() != null && other.getMimeType() != null) {
			return getMimeType().equalsIgnoreCase(other.getMimeType());
		}
		if (getExtension() != null && other.getExtension() != null) {
			return getExtension().equalsIgnoreCase(other.getExtension());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + getExtension().hashCode();
		hash = 31 * hash + getMimeType().hashCode();
		return hash;
	}
}
