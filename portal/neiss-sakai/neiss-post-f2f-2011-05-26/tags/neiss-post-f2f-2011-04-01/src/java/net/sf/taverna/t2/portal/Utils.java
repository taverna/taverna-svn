/*
 * Utility methods shared by all portlets.
 */

package net.sf.taverna.t2.portal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.embl.ebi.escience.baclava.DataThing;

/**
 *
 * @author Alex Nenadic
 */
public class Utils {

    /*
     * Saves a map of data objects for workflow input or output ports
     * to individual files in a directory dataDir (which is either 
     * <job_directory>/inputs or <job_directory>/outputs).
     * Each port gets its own sub-directory (named after the port name)
     * inside dataDir where its data gets saved.
     */
    public static void saveDataThingMapToDisk(Map<String, DataThing> dataThingMap, File dataDir){

        for (String portName : dataThingMap.keySet()){
            File portDir = new File (dataDir, portName);
            try{
                if (!portDir.exists()){
                    portDir.mkdir();
                }
                int dataDepth = calculateDataDepth(dataThingMap.get(portName).getDataObject());
                if (!saveResultsForPort(dataThingMap.get(portName).getDataObject(), portDir, dataDepth, dataDepth, "")){
                    System.out.println("Workflow Submission/Results Portlet: Failed to save individual data item for port "+ portName + " to " + portDir.getAbsolutePath());
                }
            }
            catch(Exception ex){
                System.out.println("Workflow Submission/Results Portlet: Failed to create directory "+ portDir.getAbsolutePath()+" where data value for input port "+portName+" is to be saved.");
                ex.printStackTrace();
            }
        }
    }

    /*
     * Calculate depth of a result data item.
     */
    public static int calculateDataDepth(Object dataObject) {

        if (dataObject instanceof Collection<?>){
            if (((Collection<?>)dataObject).isEmpty()){
                    return 1;
            }
            else{
                    // Calculate the depth of the first element in collection + 1
                    return calculateDataDepth(((Collection<?>)dataObject).iterator().next()) + 1;
            }
        }
        else{
            return 0;
        }
    }

    public static boolean saveResultsForPort(Object dataObject, File parentDirectory, int maxDepth, int currentDepth, String parentIndex){

        boolean success = true;

        if (maxDepth == 0){ // Result data is a single item only
            return saveDataObjectToFile(new File(parentDirectory, "Value"), dataObject);
        }
        else{
            if (currentDepth == 0){ // A leaf in the tree
                return saveDataObjectToFile(new File(parentDirectory, "Value" + parentIndex), dataObject);
            }
            else{ // Result data is a list of (lists of ... ) items
                File currentDirectory;
                if (parentIndex.equals("")){
                    currentDirectory = new File(parentDirectory, "List");
                    try{
                        currentDirectory.mkdir();
                    }
                    catch(Exception ex){
                        System.out.println("Workflow Submission/Results Portlet: Failed to create a directory "+currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                }
                else{
                    currentDirectory = new File(parentDirectory, "List" + parentIndex);
                    try{
                        currentDirectory.mkdir();
                    }
                    catch(Exception ex){
                        System.out.println("Workflow Submission/Results Portlet: Failed to create a directory "+currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                }
                for (int i=0; i < ((Collection)dataObject).size(); i++){
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i+1)).toString() : (parentIndex +"."+(i+1));
                    success = success && saveResultsForPort(((ArrayList)dataObject).get(i), currentDirectory, maxDepth, currentDepth - 1, newParentIndex);
                }
            }
        }
        return success;
    }

    public static boolean saveDataObjectToFile(File file, Object dataObject){
        if (dataObject instanceof String){
            try{
                FileUtils.writeStringToFile(file, (String)dataObject, "UTF-8");
                return true;
            }
            catch(Exception ex){
                System.out.println("Workflow Submission/Results Portlet: Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        }
        else if (dataObject instanceof byte[]){
            try{
                FileUtils.writeByteArrayToFile(file, (byte[])dataObject);
                return true;
            }
            catch(Exception ex){
                System.out.println("Workflow Submission/Results Portlet: Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        }
        else{ // unrecognised data type
            return false;
        }
    }

}
