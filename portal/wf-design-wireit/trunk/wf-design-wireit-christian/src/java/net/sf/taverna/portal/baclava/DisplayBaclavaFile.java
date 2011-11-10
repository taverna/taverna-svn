/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal.baclava;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.taverna.t2.baclava.DataThing;
import net.sf.taverna.t2.baclava.factory.DataThingXMLFactory;
import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Alex Nenadic
 */
public class DisplayBaclavaFile extends HttpServlet {

    public static final String DATA_FILE_PATH = "data_file_path"; // absolute path to the file with data
    public static final String MIME_TYPE = "mime_type";
    public static final String DATA_SIZE_IN_KB = "data_size_in_kb";
    
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>\n");
        out.println("<head>");
        out.println("<link rel=\"stylesheet\" href=\"taverna/DataTree.css\" type=\"text/css\" />");
        out.println("<script src=\"taverna/DataTree.js\" type=\"text/javascript\"></script>");
        out.println("</head>");
        out.println("<body>\n");
        
        try {
            
            // Get the Baclava file URL
            //String baclavaFileURL = URLDecoder.decode(request.getParameter("baclava_document_url"), "UTF-8");;
            String baclavaFileURL = "http://localhost:8080/wf-design-wireit-christian/Inputs/BaclavaExample.xml";
            
            // Parse the the Baclava file to produce a dataThingMap
            Map<String, DataThing> dataThingMap = null;
            try {
                
                dataThingMap = parseBaclavaFile(baclavaFileURL);
                
            } catch (MalformedURLException muex) {
                System.out.println("The Baclava file URL " + baclavaFileURL + " is malformed.");
                muex.printStackTrace();
                out.println("<p>The Baclava file URL " + baclavaFileURL + " is malformed.</p>");
                out.println("<p>The exception thrown:</p>");
                out.println("<p>"+ muex.getMessage() +"</p>");
           } catch (IOException ioex) {
                System.out.println("Failed to open Baclava file from URL " + baclavaFileURL + ".");
                ioex.printStackTrace();
                out.println("<p>Failed to open Baclava file from URL " + baclavaFileURL + ".</p>");
                out.println("<p>The exception thrown:</p>");
                out.println("<p>"+ ioex.getMessage() +"</p>");
            } catch (JDOMException jdex) {
                System.out.println("An error occured while trying to parse the data from Baclava file " + baclavaFileURL + ".");
                jdex.printStackTrace();
                out.println("<p>An error occured while trying to parse the data from Baclava file " + baclavaFileURL + ".</p>");
                out.println("<p>The exception thrown:</p>");
                out.println("<p>"+ jdex.getMessage() +"</p>");
            }
            
            // Save DataThing map to a disk so we can get to individual data files in a directory
            // Save the data structure in a temp directory
            File dataDir = new File("/tmp/blah2");
            if (dataThingMap != null) {
                System.out.println("Saving data items from Baclava document " + baclavaFileURL + " to " + dataDir.getAbsolutePath());
                if (!saveDataThingMapToDisk(dataThingMap, dataDir)){
                    out.println("<p>Failed to store data items from Baclava document to disk.</p>");
                    System.out.println("Failed to store data items from Baclava document to disk.");
                }
                else{
                    // Include the JavaScript file that creates the data tree and reacts to clicks on data nodes
                    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/taverna/DataTree.jsp");
                    dispatcher.include(request, response);
                    
                    // Create an HTML table from the data in DataThing map that uses the JavaScript above
                    String outputsTableHTML = createHTMLTableFromBaclavaFile(dataThingMap, baclavaFileURL, dataDir, request);
                    out.println(outputsTableHTML);
                }
            }          
            out.println("</body>\n");
            out.println("</html>\n");
            
        } finally {            
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>


    /**
     * Parses a baclava file containing workflow data into a port name -> DataThing map.
     */
    public static Map<String, DataThing> parseBaclavaFile(String baclavaFileURL) throws MalformedURLException, IOException, JDOMException{
         
        Map<String, DataThing> dataThingMap = null;
        InputStream inputStream = null;

        URL url = new URL(baclavaFileURL);
        inputStream = url.openStream();

        // Parse the data values from the Baclava file                           
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(inputStream);
        dataThingMap = DataThingXMLFactory.parseDataDocument(doc);

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception ex2) {
            // Do nothing
        }
        return dataThingMap;
    }
    
    /**
     * Creates a HTML table that contains a table with data structure contained
     * in a DataThing map (port name -> data) loaded from a Baclava file. Data structure 
     * nodes are linked to a data preview table where actual data values can be 
     * viewed once user clicks on the link (node) in the structure.
     *
     * The DataThing map normally contains workflow results but can contains input data as well.
     */
    private String createHTMLTableFromBaclavaFile(Map<String, DataThing> dataThingMap, String baclavaFileURL, File dataDir, HttpServletRequest request) {                                   
        
        StringBuffer dataTableHTML = new StringBuffer();

        dataTableHTML.append("<div align=\"left\" ><a target=\"_blank\" href=\""
                + baclavaFileURL
                + "\">Download the Baclava file</a><br></div>\n");
        dataTableHTML.append("</br>\n");
     
        dataTableHTML.append("<table width=\"100%\" style=\"margin-bottom:3px;\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<td valign=\"bottom\" colspan=\"2\"><div class=\"nohover_nounderline\"><b>Baclava file contents:</b></div></td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("</table>\n");

        dataTableHTML.append("<table width=\"100%\">\n");// table that contains the data links table and data preview table
        dataTableHTML.append("<tr><td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"results\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<th width=\"20%\">Port</th>\n");
        dataTableHTML.append("<th width=\"15%\">Data</th>\n");
        dataTableHTML.append("</tr>\n");
        int rowCount = 1;
        
        // Get all the ports and data associated with them
        for (Iterator i = dataThingMap.keySet().iterator(); i.hasNext();) {
            
            String portName = (String) i.next();
            DataThing dataThing = dataThingMap.get(portName);

            // Calculate the depth of the data for the port
            Object dataObject = dataThing.getDataObject();
            int dataDepth = calculateDataDepth(dataObject);
            if (rowCount % 2 != 0) {
                dataTableHTML.append("<tr>\n");
            } else {
                dataTableHTML.append("<tr style=\"background-color: #F0FFF0;\">\n");
            }
            String dataTypeBasedOnDepth;
            if (dataDepth == 0) {
                dataTypeBasedOnDepth = "single value";
            } else {
                dataTypeBasedOnDepth = "list of depth " + dataDepth;
            }
            
            // Get data's MIME type as given by the Baclava file
            String mimeType = dataThing.getMostInterestingMIMETypeForObject(dataObject);
            dataTableHTML.append("<td width=\"20%\" style=\"vertical-align:top;\">\n");
            dataTableHTML.append("<div class=\"output_name\">" + portName + "<span class=\"output_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
            dataTableHTML.append("<div class=\"output_mime_type\">" + mimeType + "</div>\n");
            dataTableHTML.append("</td>");

            // Create the data tree (with links to actual data vales)
            String dataFileParentPath = null;

            dataFileParentPath = dataDir.getAbsolutePath() + System.getProperty("file.separator") + portName;

            dataTableHTML.append("<td width=\"15%\" style=\"vertical-align:top;\"><script language=\"javascript\">" + createResultTree(dataObject, dataDepth, dataDepth, "", dataFileParentPath, mimeType, request) + "</script></td>\n");
            rowCount++;
            dataTableHTML.append("</tr>\n");
        }
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("<td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"results_data_preview\"><tr><th>Data preview</th></tr><tr><td><div style=\"vertical-align:top;\" id=\"results_data_preview\">When you select a data item - a preview of its value will appear here.</div></td></tr></table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</br>\n");

        return dataTableHTML.toString();
    }
    
    /*
     * Calculate depth of a data item from a Baclava file.
     */
    private static int calculateDataDepth(Object dataObject) {

        if (dataObject instanceof Collection<?>) {
            if (((Collection<?>) dataObject).isEmpty()) {
                return 1;
            } else {
                // Calculate the depth of the first element in collection + 1
                return calculateDataDepth(((Collection<?>) dataObject).iterator().next()) + 1;
            }
        } else {
            return 0;
        }
    }
    
    /*
     * Create a result tree in JavaScript for a result data item.
     */
    private String createResultTree(Object dataObject, int maxDepth, int currentDepth, String parentIndex, String dataFileParentPath, String mimeType, HttpServletRequest request) {

        StringBuffer resultTreeHTML = new StringBuffer();

        if (maxDepth == 0) { // Result data is a single item only
            try {
                String dataFilePath = dataFileParentPath + System.getProperty("file.separator") + "Value";
                long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                String dataFileURL = request.getContextPath() + "/FileServingServlet"
                        + "?" + DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                        + "&" + MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                        + "&" + DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
            } catch (Exception ex) {
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"\", \"results_data_preview\");\n");
            }
        } else {
            if (currentDepth == 0) { // A leaf in the tree
                try {
                    String dataFilePath = dataFileParentPath + System.getProperty("file.separator") + "Value" + parentIndex;
                    long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                    String dataFileURL = request.getContextPath() + "/FileServingServlet"
                            + "?" + DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                            + "&" + MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                            + "&" + DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                    resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value" + parentIndex + "\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
                } catch (Exception ex) {
                    resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value" + parentIndex + "\", \"\", \"results_data_preview\");\n");
                }
            } else { // Result data is a list of (lists of ... ) items
                resultTreeHTML.append("startParentNode(\"result_data\", \"List" + parentIndex + "\");\n");
                for (int i = 0; i < ((Collection) dataObject).size(); i++) {
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i + 1)).toString() : (parentIndex + "." + (i + 1));
                    resultTreeHTML.append(createResultTree(((ArrayList) dataObject).get(i),
                            maxDepth,
                            currentDepth - 1,
                            newParentIndex,
                            dataFileParentPath + System.getProperty("file.separator") + "List" + parentIndex,
                            mimeType,
                            request));
                }
                resultTreeHTML.append("endParentNode();\n");
            }
        }
        return resultTreeHTML.toString();
    }
    
    /*
     * Saves a map of data objects for workflow input or output ports
     * to individual files in a directory dataDir.
     * Each port gets its own sub-directory (named after the port name)
     * inside dataDir directory where its data gets saved.
     */

    public static boolean saveDataThingMapToDisk(Map<String, DataThing> dataThingMap, File dataDir) {

        boolean success = true;
        for (String portName : dataThingMap.keySet()) {
            File portDir = new File(dataDir, portName);
            if (!portDir.exists()) {
                portDir.mkdirs();
            }
            int dataDepth = calculateDataDepth(dataThingMap.get(portName).getDataObject());
            if (!saveDataForPort(dataThingMap.get(portName).getDataObject(), portDir, dataDepth, dataDepth, "")) {
                System.out.println("Failed to save individual data item for port " + portName + " to " + portDir.getAbsolutePath());
                success = false;
            }
        }
        return success;
    }

    /**
     * Save data for a single port in the Baclava file.
     */
    public static boolean saveDataForPort(Object dataObject, File parentDirectory, int maxDepth, int currentDepth, String parentIndex) {

        boolean success = true;

        if (maxDepth == 0) { // data item is a single item only
            return saveDataObjectToFile(new File(parentDirectory, "Value"), dataObject);
        } else {
            if (currentDepth == 0) { // A leaf in the tree
                return saveDataObjectToFile(new File(parentDirectory, "Value" + parentIndex), dataObject);
            } else { // Data item is a list of (lists of ... ) items
                File currentDirectory;
                if (parentIndex.equals("")) {
                    currentDirectory = new File(parentDirectory, "List");
                    try {
                        currentDirectory.mkdir();
                    } catch (Exception ex) {
                        System.out.println("Workflow Submission/Results Portlet: Failed to create a directory " + currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                } else {
                    currentDirectory = new File(parentDirectory, "List" + parentIndex);
                    try {
                        currentDirectory.mkdir();
                    } catch (Exception ex) {
                        System.out.println("Workflow Submission/Results Portlet: Failed to create a directory " + currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                }
                for (int i = 0; i < ((Collection) dataObject).size(); i++) {
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i + 1)).toString() : (parentIndex + "." + (i + 1));
                    success = success && saveDataForPort(((ArrayList) dataObject).get(i), currentDirectory, maxDepth, currentDepth - 1, newParentIndex);
                }
            }
        }
        return success;
    }

    public static boolean saveDataObjectToFile(File file, Object dataObject) {
        if (dataObject instanceof String) {
            try {
                FileUtils.writeStringToFile(file, (String) dataObject, "UTF-8");
                return true;
            } catch (Exception ex) {
                System.out.println("Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        } else if (dataObject instanceof byte[]) {
            try {
                FileUtils.writeByteArrayToFile(file, (byte[]) dataObject);
                return true;
            } catch (Exception ex) {
                System.out.println("Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        } else { // unrecognised data type
            return false;
        }
    }
    

}
