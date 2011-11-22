/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal.baclava;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.taverna.portal.FileServingServlet;
import net.sf.taverna.t2.baclava.DataThing;
import net.sf.taverna.t2.baclava.factory.DataThingXMLFactory;
import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
//import playground.library.functional.iterator.RecursiveFileListIterator;

/**
 *
 * @author Alex Nenadic
 */
public class DisplayBaclavaFile extends HttpServlet {
 
    private static final String APPLICATION_OCTETSTREAM = "application/octet-stream";
//    private static final String TEXT_PLAIN = "text/plain";

    private static final String MIME_TYPE_FILE_NAME = "mime_type.txt";

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
        out.println("</head>");
        out.println("<body>\n");
        
        try {       
            // Get the Baclava file URL
            String baclavaFileURLString = URLDecoder.decode(request.getParameter("baclava_document_url"), "UTF-8");;
            //String baclavaFileURL = "http://localhost:8080/wf-design-wireit-christian/Inputs/BaclavaExample.xml";           
            
            // Download the Baclava file to display
            URL baclavaFileURL = new URL(baclavaFileURLString);
            InputStream baclavaInputStream = baclavaFileURL.openStream();
            
            // Load the Baclava file into a byte array as we need to possibly read it twice
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = baclavaInputStream.read(byteChunk)) > 0) {
                bos.write(byteChunk, 0, n);
            }
            byte [] baclavaBytes = bos.toByteArray();  
            
            // Do a SHA-1 digest of the Baclava file contents to see if we already have it saved 
            // locally so we do not have to parse it again. The SHA-1 digest is used as the name  
            // for the root data directory name where individual data items per port from Baclava 
            // file are saved.
            MessageDigest sha1 = null;
            String dataDirName = null;
            try {
                System.out.println("Calculating SHA1 digest of the downloaded Baclava file.");
                sha1 = MessageDigest.getInstance("SHA1");
                DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(baclavaBytes), sha1);

                // Read the bytes and update the hash calculation
                while (dis.read() != -1);
                
                byte[] hash = sha1.digest();              
                dataDirName = byteArray2Hex(hash);

            } catch (NoSuchAlgorithmException ex) {
                System.out.println("Failed to generate SHA1 digest of the Baclava file. Using a random directory name to save data from Baclava file.");
                ex.printStackTrace();
                // Just generate a random number for the directory name
                dataDirName = new Integer(new Random().nextInt(1000000000)).toString(); 
            }

            // Save data in the data directory inside the temp directory
            File dataDir = new File(System.getProperty("java.io.tmpdir"), dataDirName);
            String outputsTableHTML = null;
            
            // Actually decided to parse the Baclava file each time - when reading data back from
            // a directory we do not get the same MIME types detected - for really short textual data
            // we get application/octet-stream and also the order of ports is different then when parsing 
            // the Baclava. So we won't read the data from the data directory where data items from the 
            // Baclava file are saved.
            
            // If the data dir already exists that means we have already parsed and
            // saved the data from the Baclava file so just generate the HTML table 
            // from the directory structure
            if (dataDir.exists()){
                System.out.println("Baclava file already saved in directory " + dataDir.getAbsolutePath() + "; no need to parse the Baclava file.");
                // Create an HTML table from the data in the data folder
                outputsTableHTML = createHTMLTableFromDataDirectory(dataDir, request);
            }
            // If the data directory does not already exist - parse the Baclava file 
            // and save the data structure from Baclava into the data directory
            else {
                // Parse the Baclava file to produce the dataThingMap
                System.out.println("Parsing the data items from Baclava document " + baclavaFileURLString);
                Map<String, DataThing> dataThingMap = null;
                try {
                    dataThingMap = parseBaclavaFile(new ByteArrayInputStream(baclavaBytes));
                } catch (MalformedURLException muex) {
                    System.out.println("The Baclava file URL " + baclavaFileURLString + " is malformed.");
                    muex.printStackTrace();
                    out.println("<p>The Baclava file URL " + baclavaFileURLString + " is malformed.</p>");
                    out.println("<p>The exception thrown:</p>");
                    out.println("<p>" + muex.getMessage() + "</p>");
                    out.println("</body>\n");
                    out.println("</html>\n");
                    return;
                } catch (IOException ioex) {
                    System.out.println("Failed to open Baclava file from URL " + baclavaFileURLString + ".");
                    ioex.printStackTrace();
                    out.println("<p>Failed to open Baclava file from URL " + baclavaFileURLString + ".</p>");
                    out.println("<p>The exception thrown:</p>");
                    out.println("<p>" + ioex.getMessage() + "</p>");
                    out.println("</body>\n");
                    out.println("</html>\n");
                    return;
                } catch (JDOMException jdex) {
                    System.out.println("An error occured while trying to parse the data from Baclava file " + baclavaFileURLString + ".");
                    jdex.printStackTrace();
                    out.println("<p>An error occured while trying to parse the data from Baclava file " + baclavaFileURLString + ".</p>");
                    out.println("<p>The exception thrown:</p>");
                    out.println("<p>" + jdex.getMessage() + "</p>");
                    out.println("</body>\n");
                    out.println("</html>\n");
                    return;
                }

                // Save DataThing map to a disk so that individual data items are saved as 
                // files in a directory structure inside the data directory and can be served
                // by the file serving servlet
                //if (!dataDir.exists()){
                    System.out.println("Saving data items from Baclava document " + baclavaFileURLString + " to " + dataDir.getAbsolutePath());
                    if (!saveDataThingMapToDisk(dataThingMap, dataDir)) {
                        System.out.println("Failed to store data items from Baclava document to disk.");
                        out.println("<p>Failed to store data items from Baclava document to disk.</p>");
                        out.println("</body>\n");
                        out.println("</html>\n");
                        return;
                    }                    
//                }
//                else{
//                    System.out.println("Baclava file already saved in directory " + dataDir.getAbsolutePath() + "; no need to parse the Baclava file.");
//                }

                // Create an HTML table from the data in DataThing map
                outputsTableHTML = createHTMLTableFromBaclavaDataThingMap(dataThingMap, dataDir, request);

            }            
          
            // Include the JavaScript files (as .jsp files) that creates the data tree 
            // that reacts to clicks on nodes in the tree
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/taverna/DataTree.jsp");
            dispatcher.include(request, response);
            dispatcher = getServletContext().getRequestDispatcher("/taverna/AjaxDataPreview.jsp");
            dispatcher.include(request, response);

            out.println("<div align=\"left\" ><a target=\"_blank\" href=\""
                    + baclavaFileURL
                    + "\">Download the Baclava file</a><br></div>\n");
            out.println("</br>\n");

            out.println(outputsTableHTML);

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
    public static Map<String, DataThing> parseBaclavaFile(InputStream baclavaFileStream) throws MalformedURLException, IOException, JDOMException{
         
        Map<String, DataThing> dataThingMap = null;

        // Parse the data values from the Baclava file                           
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(baclavaFileStream);

        dataThingMap = DataThingXMLFactory.parseDataDocument(doc);

        try {
            if (baclavaFileStream != null) {
                baclavaFileStream.close();
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
    private String createHTMLTableFromBaclavaDataThingMap(Map<String, DataThing> dataThingMap, File dataDir, HttpServletRequest request) {                                   
        
        StringBuffer dataTableHTML = new StringBuffer();

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

            // Get the data object for the port and calculate its depth
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
            
            // Get data's MIME type as given by the data object in the Baclava file
            // If a data object is a list - all items in the list will have the same MIME type!
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

    private String createHTMLTableFromDataDirectory(File dataDir, HttpServletRequest request) {

        System.out.println("Loading data items saved from the Baclava file in " + dataDir);

        StringBuffer dataTableHTML = new StringBuffer();

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

        // Get all the directories in the dataDir - they represent the ports 
        // and contain the data associated with the port
        File[] portDirectories = dataDir.listFiles(directoryFileFilter);       
        for (File portDirectory : portDirectories) {

            String portName = portDirectory.getName();

            // Create the data object from the data in the port directory
            Object dataObject = createDataObjectFromDirectory(portDirectory);
            
            // Calculate the depth of the data for the port
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

            // Get data's MIME type as given by the data object in the Baclava file
            // If a data object is a list - all items in the list will have the same MIME type.
            // MIME type is saved in a special file clled mime_type.txt inside the port directory.
            String mimeType = null;
//            RecursiveFileListIterator recursiveFileListIterator = new RecursiveFileListIterator(portDirectory);
//            if (recursiveFileListIterator.hasNext()){
//                FileInputStream fis = null;
//                try {
//                    File file = recursiveFileListIterator.next();// just get to the first file
//                    fis = new FileInputStream(file); 
//                    System.out.println("Guessing MIME type of data on port "+portDirectory.getName()+" based on file " + file.getAbsolutePath());
//                    byte[] bytes = new byte[4096];
//                    fis.read(bytes);
//                    List<MimeType> mimeTypeList = getMimeTypes(bytes);
//                    if (!mimeTypeList.isEmpty()) {
//                        mimeType = mimeTypeList.get(0).toString();
//                        System.out.println("Detected MIME type " + mimeType);
//                    }
//                    else{
//                        System.out.println("MIME type could not be detected; using text/plain.");
//                        mimeType = TEXT_PLAIN;
//                    }
//                } catch (Exception ex) {
//                    System.out.println("Failed to read file " + (String) dataObject + " to determine its MIME type.");
//                    ex.printStackTrace();
//                }       
//                finally{
//                        try {
//                            fis.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                }
//            }
            
            // We now save MIME type in a special file inside the port directory so no need to recursively
            // get to the files and use MIME magic to guess the MIME type
            File mimeTypeFile = new File(portDirectory, MIME_TYPE_FILE_NAME);
            try {
                mimeType = FileUtils.readFileToString(mimeTypeFile, "UTF-8");
            } catch (IOException ex) {
                System.out.println("Failed to read file " + mimeTypeFile.getAbsolutePath() + " to determine the MIME type for port " + portDirectory.getName());
                ex.printStackTrace();
            }            
            mimeType = (mimeType==null) ? APPLICATION_OCTETSTREAM : mimeType;
            System.out.println("Using MIME type " + mimeType + " for port " + portDirectory.getName());
     
            dataTableHTML.append("<td width=\"20%\" style=\"vertical-align:top;\">\n");
            dataTableHTML.append("<div class=\"output_name\">" + portName + "<span class=\"output_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
            dataTableHTML.append("<div class=\"output_mime_type\">" + mimeType + "</div>\n");
            dataTableHTML.append("</td>");

            // Create the data tree (with links to actual data vales)
            String dataFileParentPath = null;

            dataFileParentPath = portDirectory.getAbsolutePath();

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
        //System.out.println("maxDepth " +  maxDepth + "; currentDepti " + currentDepth + "; dataFileParentPath " + dataFileParentPath + "; mime type " + mimeType);

        StringBuffer resultTreeHTML = new StringBuffer();

        if (maxDepth == 0) { // Result data is a single item only
            try {
                String dataFilePath = dataFileParentPath + System.getProperty("file.separator") + "Value";
                long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                String dataFileURL = request.getContextPath() + "/FileServingServlet"
                        + "?" + FileServingServlet.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                        + "&" + FileServingServlet.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                        + "&" + FileServingServlet.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
                //System.out.println("dataFileURL" + dataFileURL);
            } catch (Exception ex) {
                ex.printStackTrace();
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"\", \"results_data_preview\");\n");
            }
        } else {
            if (currentDepth == 0) { // A leaf in the tree
                try {
                    String dataFilePath = dataFileParentPath + System.getProperty("file.separator") + "Value" + parentIndex;
                    long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                    String dataFileURL = request.getContextPath() + "/FileServingServlet"
                            + "?" + FileServingServlet.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                            + "&" + FileServingServlet.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                            + "&" + FileServingServlet.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                    resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value" + parentIndex + "\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
                //System.out.println("dataFileURL" + dataFileURL);
                } catch (Exception ex) {
                    ex.printStackTrace();
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
            else{
                // Save the mime type of the data object in a special file in the port directory so we can pick it up later
                String mimeType = dataThingMap.get(portName).getMostInterestingMIMETypeForObject(dataThingMap.get(portName).getDataObject());
                File mimeTypeFile = null;
                try {
                    mimeTypeFile = new File(portDir, MIME_TYPE_FILE_NAME);
                    FileUtils.writeStringToFile(mimeTypeFile, mimeType, "UTF-8");
                } catch (IOException ex) {
                    System.out.println("Failed to save mime type for port " + portName + " to " + mimeTypeFile.getAbsolutePath());
                    ex.printStackTrace();
                    success = false;
                }
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
    
    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02X", b);
        }
        return formatter.toString();
    }

    /**
     * Creates a data object structure from data contained a directory.
     */
    private Object createDataObjectFromDirectory(File portDirectory) {
    
        File[] singleDataValueFiles = portDirectory.listFiles(singleDataValueFileFilter);
        Object dataObject = null;
        
        if (singleDataValueFiles.length == 1 && singleDataValueFiles[0].getName().equals("Value")){ // There is a single item in a file called "Value" inside the directory
            dataObject = singleDataValueFiles[0].getAbsolutePath(); // we do not care what is inside the file, we are just reconstructing the data structure 
            // we'll save the absolute path in the data object so we can figure out its MIME type later on
            return dataObject;
        }
        else if(singleDataValueFiles.length > 1 || 
                (singleDataValueFiles.length == 1 && singleDataValueFiles[0].getName().equals("Value1"))){ // just one item "Value1" in the list but do not confuse it with single value result "Value"
            // Directory contains a list of files named Value1, Value2, ... 
            // We need them in the order so that, e.g., Value2 < Value10
            dataObject = new ArrayList();
            NumberedFileComparator numberedFileComparator = new NumberedFileComparator("Value");
            Arrays.sort(singleDataValueFiles, numberedFileComparator);
            for (File dataFile : singleDataValueFiles) {
                ((ArrayList) dataObject).add(dataFile.getAbsolutePath()); // we do not care what is inside the file, we are just reconstructing the data structure
            }
            return dataObject;
        }
        else{ // list of items - go recursively
            dataObject = new ArrayList();
            File[] listDirectories = portDirectory.listFiles(directoryFileFilter); 
            
            // There is just one directory inside named "List"
            if (listDirectories.length == 1 && listDirectories[0].getName().equals("List")){ 
                // This is just a container for the list so just go inside this directory
                return createDataObjectFromDirectory(listDirectories[0]);
            }
            else{
                // Directory contains a list of directories named List1, List2, ... 
                // or List1.1, List1.2, etc. 
                // We need them in the order so that, e.g., List1.2 < List1.10
                NumberedFileComparator numberedFileComparator = new NumberedFileComparator("List");
                Arrays.sort(listDirectories, numberedFileComparator);
                for (File directory : listDirectories) {
                    ((ArrayList) dataObject).add(createDataObjectFromDirectory(directory));
                }  
            }
            return dataObject;
        }
    }
    
    private FileFilter singleDataValueFileFilter = new FileFilter() {

        public boolean accept(File file) {
            return file.getName().startsWith("Value") && !file.getName().contains("thumbnail");
        }
    };
    private FileFilter directoryFileFilter = new FileFilter() {

        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    /**
     * Compares file/directory names like List1.2, List1.10, ... etc. so
     * that List1.2 comes before (i.e. "is less than") List1.10.
     * 
     * Similar for strings starting with word "Value", e.g. Value1, Value2, Value10, ...
     * These can also have "_thumbnail.jpg" appended at the end for thumbnail images.
     */
    private class NumberedFileComparator implements Comparator {
        
        private String prefix;
        private String suffix = "_thumbnail";
        
        public NumberedFileComparator(String prefix){
            this.prefix = prefix;
        }

        public int compare(Object o1, Object o2) {
            
            String f1 = ((File) o1).getName().substring(prefix.length()); // eliminate the file name prefix from the string
            String f2 = ((File) o2).getName().substring(prefix.length());
            
            // Get rid of any "_thumbnail" suffixes
            if (f1.contains(suffix)){
                f1 = f1.substring(0, f1.indexOf(suffix));
            }
            if (f2.contains(suffix)){
                f2 = f2.substring(0, f2.indexOf(suffix));
            }
            
            // Next eliminate "." and collect numbers only from the rest of the string
            String[] num1List = f1.split("[.]");
            String[] num2List = f2.split("[.]");
            
            int num1 = 0;
            int num2 = 0;
            
            for (int i = 0; i < num1List.length; i ++){
                num1 = 10 * num1 + new Integer(num1List[i]).intValue();
            }
            for (int i = 0; i < num2List.length; i ++){
                num2 = 10 * num2 + new Integer(num2List[i]).intValue();
            }        
            
            return num1 - num2;
        }
        
    }
    
    public static List<MimeType> getMimeTypes(byte[] bytes) {
        List<MimeType> mimeList = new ArrayList<MimeType>();
        MimeUtil2 mimeUtil = new MimeUtil2();
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtraMimeTypes");
        try {
            Collection<MimeType> mimeTypes2 = mimeUtil.getMimeTypes(bytes);
            mimeList.addAll(mimeTypes2);

            // Hack for SVG that seems not to be recognised
            String bytesString = new String(bytes, "UTF-8");
            if (bytesString.contains("http://www.w3.org/2000/svg")) {
                MimeType svgMimeType = new MimeType("image/svg+xml");
                if (!mimeList.contains(svgMimeType)) {
                    mimeList.add(svgMimeType);
                }
            }
            if (mimeList.isEmpty()) { // if it is not recognised
                mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
            }

        } catch (IOException ex) {
            mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
        }

        return mimeList;
    }

}
