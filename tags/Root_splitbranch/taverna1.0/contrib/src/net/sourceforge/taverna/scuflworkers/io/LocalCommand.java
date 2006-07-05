package net.sourceforge.taverna.scuflworkers.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor executes a commandline and returns the response 
 * as a String.  Code borrowed from: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * 
 * @author Mark
 * @version $Revision: 1.5 $
 * 
 * @tavinput command  The command to be executed.
 * @tavinput args	  A list of  arguments to be passed to the command.
 * @tavoutput result  The text results returned by the command.
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
        String[] args = inAdapter.getStringArray("args");
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
            
            // concatenate the arrays
            int argSize = cmdArray.length + args.length;
            ArrayList appArray = new ArrayList(argSize);
            for (int i=0; i < cmdArray.length; i++){
            	appArray.add(cmdArray[i]);
            }
            
            for(int i=0; i < args.length; i++){
            	appArray.add(args[i]);
            }
            
        	String[] applist = new String[argSize];
            appArray.toArray(applist);
            proc = rt.exec(applist);
            
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
        return new String[]{"command","args"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","l('text/plain')"};
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
