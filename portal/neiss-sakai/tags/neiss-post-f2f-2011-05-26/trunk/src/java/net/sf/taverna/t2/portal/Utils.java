/*
 * Utility methods shared by all portlets.
 */

package net.sf.taverna.t2.portal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

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

    public static Element getTopDataflow(Element element) {
        Element result = null;
        for (Object elObj : element.getChildren(Constants.DATAFLOW_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE)) {
            Element dataflowElement = (Element) elObj;
            if (Constants.DATAFLOW_ROLE_TOP.equals(dataflowElement.getAttribute(Constants.DATAFLOW_ROLE).getValue())) {
                result = dataflowElement;
            }
        }
        return result;
    }

    /*
     * Given an <annotations> element from the .t2flow file,
     * it finds the latest <net.sf.taverna.t2.annotation.AnnotationAssertionImpl>
     * element regardless of its location in the <annotations>, element whose
     * <annotationBean> sub-element has a class attribute that matches the
     * passed value. It then returns the value of the <text> element inside that
     * <annotationBean> element.
     */
    public static String getLatestAnnotationAssertionImplElementValue(Element annotationsElement, String annotationBeanClassName, String workflowFileName) {

        //System.out.println("Getting annotations with class='" + annotationBeanClassName + "' from element " + new XMLOutputter().outputString(annotationsElement) + "\n");

        // Select all <net.sf.taverna.t2.annotation.AnnotationAssertionImpl>
        // elements no matter where they are located inside the <assertions> element passed.
        List<Element> annotationAssertionImplElements = null;
        try {
            //JDOMXPath path = new JDOMXPath(".//"+Constants.ANNOTATION_ASSERTION_IMPL_ELEMENT);
            //annotationAssertionImplElements = path.selectNodes(annotationsElement);

            annotationAssertionImplElements = XPath.selectNodes(annotationsElement, ".//" + Constants.ANNOTATION_ASSERTION_IMPL_ELEMENT);
        } catch (Exception ex) {
            System.out.println("Workflow Submission Portlet: Failed to parse the annotations element when looking for " + annotationBeanClassName + " in worklow " + workflowFileName + ".");
            ex.printStackTrace();
            return null;
        }

        // Loop over all the annotation assertion implementation elements
        // and find the latest that has an annotation bean whose class
        // matches the one we are looking for.
        String latestValue = null;
        Date latestDate = new Date(0);
        if (annotationAssertionImplElements != null) {
            for (Element annotationAssertionImplElement : annotationAssertionImplElements) {

                Element annotationBeanElement = annotationAssertionImplElement.getChild(Constants.ANNOTATION_BEAN_ELEMENT);

                Date date = null;
                String pattern = "yyyy-MM-dd HH:mm:ss.SSS z";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                if (annotationBeanElement.getAttributeValue(Constants.ANNOTATION_BEAN_ELEMENT_CLASS_ATTRIBUTE).equals(annotationBeanClassName)) {
                    String value = annotationBeanElement.getChildText(Constants.TEXT_ELEMENT);

                    try {
                        date = format.parse(annotationAssertionImplElement.getChildText(Constants.DATE_ELEMENT));
                        if (latestDate.before(date)) {
                            latestValue = value;
                            latestDate = date;
                        }
                    } catch (ParseException ex) {
                        System.out.println("Workflow Submission Portlet: Failed to parse the annotation bean date for " + annotationBeanClassName + " in workflow " + workflowFileName + ". Skipping this element.");
                        ex.printStackTrace();
                        continue;
                    }
                }
            }
        }
        return latestValue;
    }

    /*
     * Parse workflow stream and return an XML document.
     */
    public static Document parseWorkflow(InputStream inputStream) throws JDOMException, IOException {
        Document workflowDocument;
        SAXBuilder builder = new SAXBuilder();
        workflowDocument = builder.build(inputStream);
        try {
            inputStream.close();
        } catch (Exception ex) {
            // Ignore
        }
        return workflowDocument;
    }
}
