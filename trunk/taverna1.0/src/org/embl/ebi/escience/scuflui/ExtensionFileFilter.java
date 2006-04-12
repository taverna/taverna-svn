/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.scufl.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import javax.swing.filechooser.*;

/**
 * A FileFilter implementation that can be configured to show only specific file
 * suffixes.
 * 
 * @author Tom Oinn
 */
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
	String[] allowedExtensions;

	public ExtensionFileFilter(String[] allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = getExtension(f);
		if (extension != null) {
			for (int i = 0; i < allowedExtensions.length; i++) {
				if (extension.equalsIgnoreCase(allowedExtensions[i])) {
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

	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter for extensions : ");
		for (int i = 0; i < allowedExtensions.length; i++) {
			sb.append(allowedExtensions[i]);
			if (i < allowedExtensions.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

}
