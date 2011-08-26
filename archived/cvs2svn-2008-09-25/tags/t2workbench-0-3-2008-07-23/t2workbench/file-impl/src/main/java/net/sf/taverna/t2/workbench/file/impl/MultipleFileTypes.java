package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.file.FileType;

public class MultipleFileTypes extends FileFilter {
	private String description;
	private final Set<FileType> fileTypes;

	public MultipleFileTypes(Set<FileType> fileTypes, String description) {
		this.fileTypes = fileTypes;
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}

		String lowerFileName = file.getName().toLowerCase();
		for (FileType fileType : fileTypes) {
			if (lowerFileName.endsWith(fileType.getExtension())) {
				return true;
			}
		}
		return false;
	}

}