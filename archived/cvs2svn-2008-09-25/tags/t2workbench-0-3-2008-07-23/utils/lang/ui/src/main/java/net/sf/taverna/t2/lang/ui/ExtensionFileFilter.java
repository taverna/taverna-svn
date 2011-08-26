/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.lang.ui;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter implementation that can be configured to show only specific file
 * suffixes.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public class ExtensionFileFilter extends FileFilter {
	List<String> allowedExtensions;

	public ExtensionFileFilter(List<String> allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	public ExtensionFileFilter(String[] allowedExtensions) {
		this.allowedExtensions = Arrays.asList(allowedExtensions);
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = getExtension(f);
		if (extension != null) {
			for (String allowedExtension : allowedExtensions) {
				if (extension.equalsIgnoreCase(allowedExtension)) {
					return true;
				}
			}
		}
		return false;
	}

	String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	@Override
	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter for extensions : " );
		for (int i = 0; i < allowedExtensions.size(); i++) {
			sb.append(allowedExtensions.get(i));
			if (i < allowedExtensions.size() - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
