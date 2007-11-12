/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * class for filtering R files
 * 
 * @author Ingo Wassink
 * 
 */
public class RshellFileFilter extends FileFilter {

	/**
	 * Method for checking whether a file is accepted
	 * 
	 * @param file
	 *            the file to be checked
	 * @return true if file ends with ".r"
	 */
	public boolean accept(File file) {
		return file.getName().toLowerCase().endsWith(".r");
	}

	/**
	 * Method for getting the description
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return ".r (R files)";
	}

}
