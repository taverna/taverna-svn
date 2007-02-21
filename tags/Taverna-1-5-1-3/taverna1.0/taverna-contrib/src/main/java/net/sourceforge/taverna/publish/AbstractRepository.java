package net.sourceforge.taverna.publish;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

/**
 * This class provides base-level functionality for repositories.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public abstract class AbstractRepository implements Repository {
	protected FileSystemManager fsManager = null;

	protected FileSystem fileSystem = null;

	protected FileObject root = null;

	/**
	 * Constructor
	 * 
	 * @return
	 */
	public FileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * This method sets the file system.
	 * 
	 * @param fileSystem
	 */
	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	private String baseURL;

	private String name;

	/**
	 * @see net.sourceforge.taverna.publish.Repository#getBaseURL()
	 */
	public String getBaseURL() {
		return this.baseURL;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#setBaseURL(java.lang.String)
	 */
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;

	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#publish(java.io.File[],
	 *      java.io.File)
	 */
	public abstract void publish(FileObject[] filelist, FileObject startingDir) throws PublicationException;

	/**
	 * This method gets the file system manager.
	 * 
	 * @return
	 */
	public FileSystemManager getFsManager() {
		return fsManager;
	}

	/**
	 * This method sets the file system manager.
	 * 
	 * @param fsManager
	 */
	public void setFsManager(FileSystemManager fsManager) {
		this.fsManager = fsManager;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#getRoot()
	 */
	public FileObject getRoot() {
		return root;
	}

	/**
	 * This method sets the root of the repository.
	 * 
	 * @param root
	 */
	public void setRoot(FileObject root) {
		this.root = root;
	}

	/**
	 * This method returns a list a files that have been filtered
	 * 
	 * @param startingDir
	 * @param filter
	 * @param searchRecursively
	 * @return
	 */
	public FileObject[] getFileList(FileObject startingDir, FileSelector selector, boolean searchRecursively) {
		FileObject[] filelist = null;
		try {
			filelist = startingDir.findFiles(selector);
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return filelist;
	}

	/**
	 * This method gets all files
	 * 
	 * @return
	 */
	public FileObject[] getFileList() {
		AllFileSelector selector = new AllFileSelector();
		FileObject[] filelist = getFileList(this.root, selector, true);
		return filelist;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#searchByFileName(java.io.File,
	 *      java.lang.String, boolean)
	 */
	public FileObject[] searchByFileName(FileObject startingDir, String regex, boolean srchRecursively) {
		FileObject[] filelist = null;
		filelist = this.getFileList(startingDir, new RegexFileSelector(regex, srchRecursively), srchRecursively);
		return filelist;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#searchByWorkFlowDescription(java.io.File,
	 *      java.lang.String, boolean)
	 */
	public FileObject[] searchByWorkFlowDescription(FileObject startingDir, String regex, boolean srchRecursively) {
		FileObject[] filelist = null;
		filelist = this.getFileList(startingDir, new XPathFileSelector("//description"), srchRecursively);
		return filelist;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#searchByWorkFlowAuthor(java.io.File[],
	 *      java.lang.String, boolean)
	 */
	public FileObject[] searchByWorkFlowAuthor(FileObject root, String authorName, boolean srchRecursively) {
		FileObject[] filelist = null;
		filelist = this.getFileList(root, new XPathFileSelector("//author", srchRecursively), srchRecursively);
		return filelist;
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#delete(java.io.File[])
	 */
	public void delete(FileObject[] filelist) throws PublicationException {
		try {
			for (int i = 0; i < filelist.length; i++) {
				filelist[i].delete();
			}
		} catch (FileSystemException ex) {
			throw new PublicationException(ex);
		}

	}

	protected String LINE_ENDING = System.getProperty("line.separator");

}