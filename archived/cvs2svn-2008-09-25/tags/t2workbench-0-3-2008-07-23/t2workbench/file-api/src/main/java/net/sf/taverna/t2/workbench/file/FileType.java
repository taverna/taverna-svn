package net.sf.taverna.t2.workbench.file;

public abstract class FileType {

	public abstract String getExtension();

	public abstract String getMimeType();

	public abstract String getDescription();

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof FileType)) {
			return false;
		}
		FileType other = (FileType) obj;
		return getExtension().equalsIgnoreCase(other.getExtension())
				&& getMimeType().equals(other.getMimeType());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + getExtension().hashCode();
		hash = 31 * hash + getMimeType().hashCode();
		return hash;
	}
}
