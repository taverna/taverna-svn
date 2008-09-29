package net.sourceforge.taverna.publish;

import org.apache.commons.vfs.FileObject;

/**
 * This interface is used to filter a list of files by an arbitrary set of 
 * criteria.  The criteria are applied in the accept method.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public interface FileFilter {
    
    /**
     * This method determines whether or not a fileobject meets the selection criteria.
     * @param obj  The file being tested.
     * @return
     */
    public boolean accept(FileObject obj);

}
