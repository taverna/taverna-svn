package org.apache.bsf;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class provides internal access to BSFManager internals
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class ExtendedBSFManager extends BSFManager {
    
    /**
     * This method returns a list of available Scripting engines.
     * @return
     */
    public Vector getProcessorNameList(){
        Iterator it = this.loadedEngines.keySet().iterator();
        Vector procList = new Vector();
        String currName = null;
        while(it.hasNext()){
            currName = (String)it.next();
            procList.add(currName);
        }
        
        return procList;
    }

}
