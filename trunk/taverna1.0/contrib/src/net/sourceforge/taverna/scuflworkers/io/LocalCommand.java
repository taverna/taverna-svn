package net.sourceforge.taverna.scuflworkers.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class executes a commandline and returns the response 
 * as a String.  Code borrowed from: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class LocalCommand implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        Map outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

        String cmd = inAdapter.getString("command");
        if (cmd == null || cmd.equals("")){
            throw new TaskExecutionException("The 'command' port cannot be null.");
        }
        Process proc = null;
        try {
            Runtime rt = Runtime.getRuntime();
            
            String osName = System.getProperty("os.name" );
            String[] cmdArray = null;
            if (osName.equals("Windows NT") || osName.equals("Windows XP")){
                cmdArray = new String[]{"cmd.exe","/c",cmd};
            }else if (osName.equals("Windows 95")){
                cmdArray = new String[]{"command.exe","/c",cmd};
            }else {//TODO: investigate if this will work in Linux and OSX
                cmdArray = new String[]{cmd};
            }
            
            proc = rt.exec(cmdArray);
            
            
            //Get the input stream and read from it
            InputStream in = proc.getInputStream();
            
            int c;
            StringBuffer sb = new StringBuffer();
            while ((c = in.read()) != -1) {
                sb.append((char)c);
            }
            in.close();
            outAdapter.putString("result",sb.toString());
        } catch (IOException e) {           
           throw new TaskExecutionException(e);
           
        }

        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"command"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"result"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[]{"'text/plain'"};
    }

}
