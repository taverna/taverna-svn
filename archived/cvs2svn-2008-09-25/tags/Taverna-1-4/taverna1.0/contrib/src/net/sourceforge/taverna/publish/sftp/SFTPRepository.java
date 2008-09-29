package net.sourceforge.taverna.publish.sftp;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileUtil;

import net.sourceforge.taverna.publish.AbstractRepository;
import net.sourceforge.taverna.publish.PublicationException;
import net.sourceforge.taverna.publish.Repository;

/**
 * This class represents a Secure FTP repository.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class SFTPRepository extends AbstractRepository implements Repository {

    /**
     * Constructor
     * @param root
     */
    public SFTPRepository(String root){
        try {
            this.root = this.fsManager.resolveFile(root);            
            
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * @see net.sourceforge.taverna.publish.Repository#publish(org.apache.commons.vfs.FileObject[], org.apache.commons.vfs.FileObject)
     */
    public void publish(FileObject[] filelist, FileObject startingDir)
            throws PublicationException {
        String filedir = startingDir.getName().getPath();
        FileObject destFile = null;
        try {
            for (int i = 0; i < filelist.length; i++) {
                //filelist[i].getName().
                destFile = this.fsManager.resolveFile(filedir + LINE_ENDING );
                
                FileUtil.copyContent(filelist[i], startingDir);
            }
        } catch (IOException e) {
            throw new PublicationException(e);
        }
    


    }

}
