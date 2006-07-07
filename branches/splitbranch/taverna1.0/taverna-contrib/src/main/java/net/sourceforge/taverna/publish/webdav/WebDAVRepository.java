package net.sourceforge.taverna.publish.webdav;

import java.io.IOException;

import net.sourceforge.taverna.publish.AbstractRepository;
import net.sourceforge.taverna.publish.PublicationException;
import net.sourceforge.taverna.publish.Repository;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileUtil;
import org.apache.commons.vfs.VFS;

//import org.apache.commons.vfs.provider.webdav.WebDavFileSystem;

/**
 * This class represents a webdav workflow repository.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class WebDAVRepository extends AbstractRepository implements Repository {

	/**
	 * Constructor
	 * 
	 * @param rootFileUrl
	 */
	public WebDAVRepository(String rootFileUrl) {

		try {
			this.setFsManager(VFS.getManager());
			this.root = this.fsManager.resolveFile(rootFileUrl);
			this.fsManager.createFileSystem(root);
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#publish(java.io.File[],
	 *      java.io.File)
	 */
	public void publish(FileObject[] filelist, FileObject startingDir) throws PublicationException {
		String filedir = startingDir.getName().getPath();
		FileObject destFile = null;
		try {
			for (int i = 0; i < filelist.length; i++) {
				// filelist[i].getName().
				destFile = this.fsManager.resolveFile(filedir + LINE_ENDING);

				FileUtil.copyContent(filelist[i], startingDir);
			}
		} catch (IOException e) {
			throw new PublicationException(e);
		}

	}

}
