package net.sourceforge.taverna.scuflworkers.io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class gets a list of files on a local directory.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class FileListByExtTask implements LocalWorker {
    
    public FileListByExtTask(){
        
    }

    
    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
       DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
       HashMap outputMap = new HashMap();
       DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
       String ext = inAdapter.getString("extension");
       String dir = inAdapter.getString("directory");
       
       
       if (ext == null || ext.equals("")){
           throw new TaskExecutionException("The 'ext' parameter cannot be null.  Please enter a valid file extension.");
       }
       
       
       if (dir == null || dir.equals("")){
           throw new TaskExecutionException("The 'directory' parameter cannot be null.  Please enter a valid file directory.");  
           
       }
       
       File dirObj = new File(dir);
       
       if (!dirObj.exists()){
           throw new TaskExecutionException("The 'directory' parameter specified:" + dir +"does not exist.  Please enter a valid file directory.");  
       }
       
       File[] fileObjList = dirObj.listFiles(new FileExtFilter(ext));
       String[] fileList = new String[fileObjList.length];
       for(int i=0; i < fileList.length; i++){
           fileList[i] = fileObjList[i].getAbsolutePath();
       }
       
       outAdapter.putStringArray("filelist",fileList);
       
       return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
       return new String[]{"directory","extension"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
       return new String[]{"filelist"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[]{"l('text/plain')"};
    }
    
    /**
     * A simple file extension filter.  If the name of the file ends
     * with the extension supplied in the constructor then the file
     * will be accepted.
     */
    class FileExtFilter implements FileFilter{
        
        public FileExtFilter(String ext){
            this.ext = ext;
        }
             
        public boolean accept(File file){
            return file.getName().endsWith(ext);
        }
       
        
        String ext = null;
    }

}
