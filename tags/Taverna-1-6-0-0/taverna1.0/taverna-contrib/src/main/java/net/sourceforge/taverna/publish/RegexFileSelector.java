package net.sourceforge.taverna.publish;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;

/**
 * This class selects files whose name matches the regular expression provided
 * in the constructor.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class RegexFileSelector implements FileSelector {

	private String regex;

	private boolean recurseDirectories = false;

	/**
	 * Constructor
	 * 
	 * @param regex
	 *            The regular expression to be applied to the files.
	 */
	public RegexFileSelector(String regex) {
		this.regex = regex;
	}

	/**
	 * Constructor
	 * 
	 * @param regex
	 * @param recurseDirectories
	 */
	public RegexFileSelector(String regex, boolean recurseDirectories) {
		this.regex = regex;
		this.recurseDirectories = recurseDirectories;
	}

	/**
	 * @see org.apache.commons.vfs.FileSelector#includeFile(org.apache.commons.vfs.FileSelectInfo)
	 */
	public boolean includeFile(FileSelectInfo info) throws Exception {
		FileObject file = info.getFile();
		return file.getName().toString().matches(this.regex);
	}

	/**
	 * @see org.apache.commons.vfs.FileSelector#traverseDescendents(org.apache.commons.vfs.FileSelectInfo)
	 */
	public boolean traverseDescendents(FileSelectInfo info) throws Exception {
		return this.recurseDirectories;
	}

}