package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.file.FileType;

public class FileTypeFileFilter extends FileFilter {
	private final FileType fileType;

	public FileTypeFileFilter(FileType fileType) {
		this.fileType = fileType;
	}

	@Override
	public String getDescription() {
		return fileType.getDescription();
	}

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().toLowerCase().endsWith(
				"." + fileType.getExtension());
	}

	public FileType getFileType() {
		return fileType;
	}

}