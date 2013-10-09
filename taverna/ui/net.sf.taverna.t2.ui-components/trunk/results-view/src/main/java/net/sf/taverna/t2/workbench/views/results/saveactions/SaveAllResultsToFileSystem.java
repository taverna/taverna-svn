/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import uk.org.taverna.databundle.DataBundles;

/**
 * Stores results to the file system.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class SaveAllResultsToFileSystem extends SaveAllResultsSPI {

	public SaveAllResultsToFileSystem(){
		super();
		putValue(NAME, "Save as directory");
		putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
	}

	public AbstractAction getAction() {
		return new SaveAllResultsToFileSystem();
	}

	/**
	 * Saves the result data as a file structure
	 * @throws IOException
	 */
	protected void saveData(File directory) throws IOException {
		if (directory.exists() && !directory.isDirectory()) {
			throw new IOException(directory.getName() + " is not a directory.");
		}
		for (String portName : chosenReferences.keySet()) {
			writeToFileSystem(chosenReferences.get(portName), new File(directory, portName));
		}
	}

	/**
	 * Write a specific object to the filesystem this has no access to metadata
	 * about the object and so is not particularly clever. A File object
	 * representing the file or directory that has been written is returned.
	 */
	public File writeToFileSystem(Path source, File destination) throws IOException {
		destination.mkdirs();
		if (Files.isDirectory(source)) {
			DataBundles.copyRecursively(source, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} else if (Files.exists(source)){
			Files.copy(source, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return destination;
	}

	@Override
	protected int getFileSelectionMode() {
		return JFileChooser.DIRECTORIES_ONLY;
	}

	@Override
	protected String getFilter() {
		return null;
	}
}
