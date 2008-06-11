package net.sf.taverna.t2.workbench.file;

import java.io.File;

public class OverwriteException extends SaveException {
	private final File file;

	public OverwriteException(File file) {
		super("Save would overwrite existing file " + file);
		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
