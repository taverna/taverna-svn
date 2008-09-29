package net.sourceforge.taverna.publish.p2p;

import java.io.IOException;

import net.sourceforge.taverna.publish.AbstractRepository;
import net.sourceforge.taverna.publish.PublicationException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileUtil;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class JxtaRepository extends AbstractRepository {

	/**
	 * Constructor
	 * 
	 * @param localShareRoot
	 */
	public JxtaRepository(FileObject localShareRoot) {
		this.setRoot(localShareRoot);
	}

	/**
	 * @see net.sourceforge.taverna.publish.Repository#publish(org.apache.commons.vfs.FileObject[],
	 *      org.apache.commons.vfs.FileObject)
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
