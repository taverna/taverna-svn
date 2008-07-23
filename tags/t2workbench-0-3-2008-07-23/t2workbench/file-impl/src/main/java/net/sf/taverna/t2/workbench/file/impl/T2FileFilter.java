/**
 * 
 */
package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class T2FileFilter extends FileFilter {
	@Override
	public boolean accept(final File file) {
		return file.getName().toLowerCase().endsWith(".t2flow");
	}

	@Override
	public String getDescription() {
		return "Taverna 2 workflows";
	}
}