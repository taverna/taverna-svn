package net.sourceforge.taverna.publish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

/**
 * This class manages all publishing activities.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class RepositoryManager {
	FileSystemManager fsManager = null;

	public RepositoryManager() {
		try {
			fsManager = VFS.getManager();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method gets an instance of a repository.
	 * 
	 * @param repositoryClass
	 *            A fully qualified classname for the repository.
	 * @return
	 */
	public Repository getRepositoryInstance(String repositoryClass) {
		Repository rep = null;
		try {
			rep = (Repository) Class.forName(repositoryClass).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rep;
	}

	/**
	 * This method gets an instance of a repository based on the name of the
	 * repository.
	 * 
	 * @param repositoryName
	 * @return
	 */
	public Repository getInstance(String repositoryName) {
		Repository rep = null;
		Preferences prefs = Preferences.userNodeForPackage(Repository.class);

		return rep;
	}

	/**
	 * This method gets all repositories defined on the local machine.
	 * 
	 * @return
	 */
	public Repository[] getAllRepositories() {
		Repository[] replist = null;

		return replist;
	}

	/**
	 * This method searches all repositories for a file with a specific name.
	 * 
	 * @param regex
	 *            The regular expression for the filename.
	 * @return
	 */
	public FileObject[] searchAllRepositoriesByFileName(String regex) {
		FileObject[] filelist = null;
		Repository[] replist = getAllRepositories();
		Repository rep = null;
		ArrayList completeList = new ArrayList();
		for (int i = 0; i < replist.length; i++) {
			rep = replist[i];
			filelist = rep.searchByFileName(rep.getRoot(), regex, true);
			completeList.addAll(Arrays.asList(filelist));
		}
		FileObject[] allFiles = new FileObject[completeList.size()];
		completeList.toArray(allFiles);
		return allFiles;
	}

	/**
	 * This method searches respositories by the workflow author.
	 * 
	 * @param author
	 * @param searchRecursively
	 * @return
	 */
	public FileObject[] searchAllRepositoriesByAuthor(String author, boolean searchRecursively) {
		FileObject[] filelist = null;
		Repository[] replist = getAllRepositories();
		Repository rep = null;
		ArrayList completeList = new ArrayList();
		for (int i = 0; i < replist.length; i++) {
			rep = replist[i];
			filelist = rep.searchByWorkFlowAuthor(rep.getRoot(), author, true);
			completeList.addAll(Arrays.asList(filelist));
		}
		FileObject[] allFiles = new FileObject[completeList.size()];
		completeList.toArray(allFiles);
		return allFiles;
	}

	/**
	 * This method searches all repositories for a workflows with a matching
	 * description.
	 * 
	 * @param description
	 * @param searchRecursively
	 * @return
	 */
	public FileObject[] searchAllRepositoriesByDescription(String description, boolean searchRecursively) {
		FileObject[] filelist = null;
		Repository[] replist = getAllRepositories();
		Repository rep = null;
		ArrayList completeList = new ArrayList();
		for (int i = 0; i < replist.length; i++) {
			rep = replist[i];
			filelist = rep.searchByWorkFlowDescription(rep.getRoot(), description, searchRecursively);
			completeList.addAll(Arrays.asList(filelist));
		}
		FileObject[] allFiles = new FileObject[completeList.size()];
		completeList.toArray(allFiles);
		return allFiles;

	}

	/**
	 * This method searches for a repository given a specific filename regular
	 * expression.
	 * 
	 * @param repositoryName
	 * @param fileRegex
	 * @param startingDir
	 * @param searchRecursively
	 * @return
	 * @throws FileSystemException
	 */
	public FileObject[] searchRepositoryByFileName(String repositoryName, String fileRegex, String startingDir,
			boolean searchRecursively) throws FileSystemException {
		FileObject[] filelist = null;
		Repository rep = getInstance(repositoryName);
		FileObject root = fsManager.resolveFile(startingDir);
		filelist = rep.searchByFileName(root, fileRegex, searchRecursively);
		return filelist;
	}

	/**
	 * This method searches the repository by the author's name.
	 * 
	 * @param repositoryName
	 * @param author
	 * @param startingDir
	 * @param searchRecursively
	 * @return
	 * @throws FileSystemException
	 */
	public FileObject[] searchRepositoryByAuthor(String repositoryName, String author, String startingDir,
			boolean searchRecursively) throws FileSystemException {
		FileObject[] filelist = null;
		Repository rep = getInstance(repositoryName);
		FileObject root = fsManager.resolveFile(startingDir);
		filelist = rep.searchByWorkFlowAuthor(root, author, searchRecursively);
		return filelist;
	}

	/**
	 * This method searches a repository by description.
	 * 
	 * @param repositoryName
	 * @param author
	 * @return
	 * @throws FileSystemException
	 */
	public FileObject[] searchRepositoryByDescription(String repositoryName, String startingDir, String description,
			boolean searchRecursively) throws FileSystemException {
		FileObject[] filelist = null;
		Repository rep = getInstance(repositoryName);
		FileObject root = fsManager.resolveFile(startingDir);
		filelist = rep.searchByWorkFlowDescription(root, description, searchRecursively);
		return filelist;
	}

}
