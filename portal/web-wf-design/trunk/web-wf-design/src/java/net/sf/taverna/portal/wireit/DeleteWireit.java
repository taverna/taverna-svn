package net.sf.taverna.portal.wireit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

/**
 * Deletes a Wiring from the SQL database.
 * 
 * @author Christian
 */
public class DeleteWireit extends WireitSQLBase {

    public static final String JSON_WIRINGS_DIR_PARAMETER = "JSON_WIRINGS_DIR";
    public static final String SAVE_TO_PARAMETER = "SAVE_TO";
    public static final String SAVE_TO_FILESYSTEM = "filesystem";
    public static final String SAVE_TO_DATABASE = "database";
    private File jsonWiringsDir = null; // directory where we save JSON wirings as individual files
    private String SAVE_TO; // wirings will be saved to/loaded from filesystem or database based on the value of this context parameter

    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    public DeleteWireit() throws ServletException{
        super();
    }

    @Override
    public void init(){
        String jsonWiringsDirPath = getServletContext().getInitParameter(JSON_WIRINGS_DIR_PARAMETER);
        SAVE_TO = getServletContext().getInitParameter(SAVE_TO_PARAMETER);
        if (SAVE_TO == null){
            throw new RuntimeException("Method of saving JSON wirings (filesystem or database) has not been configured in web.xml. Check context parameter " + SAVE_TO_PARAMETER +".");
        }
        else{
            if (SAVE_TO.toLowerCase().equals(SAVE_TO_FILESYSTEM)) {
                if (jsonWiringsDirPath != null) {
                    jsonWiringsDir = new File(jsonWiringsDirPath);
                    if (!jsonWiringsDir.exists()) {
                        jsonWiringsDir.mkdirs();
                    }
                } else {
                    throw new RuntimeException("Directory where to save to/load from JSON wirings has not been configured in web.xml. Check context parameter " + JSON_WIRINGS_DIR_PARAMETER + ".");
                }
            }
        }
    }
    
    /**
     * Deletes a Wiring.
     * 
     * If a wiring exists in the database with a name and language that matches the part of the URL after the ?
     * It will be deleted.
     * <p>
     * No Exception is thrown if there is no wiring of that name or if there is nothing after the ?
     * <p>
     * Case sensitivity depends on how the underlying database handles =.
     * 
     * @param request Interesting part here is the Query String (part after the ?) This is assumed to be the name.
     * @param response "{\"error\":null}" in "text/x-json;charset=UTF-8" format.
     * @throws ServletException Thrown if the SQL delete fails. 
     * @throws IOException Thrown if writing the response fails.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println();
        System.out.println((new Date()) + "in DeleteWireit.doGet");
             
        String name = request.getParameter("name");
        String encodedName = URLEncoder.encode(name);
        String language = request.getParameter("language");
       try {
            if (SAVE_TO.toLowerCase().equals(SAVE_TO_FILESYSTEM)) {
                deleteJSONWiringFromFileSystem(encodedName); // delete from the filesystem
            } else {
                deleteJSONWiringFromDatabase(encodedName, language); // delete from the database
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex);
        }
        
        response.setContentType("text/x-json;charset=UTF-8");  
        PrintWriter out = response.getWriter();
        out.println("{\"error\":null}");             
        
    }
               
    /**
     * Deletes the wiring of this name if it exists.
     * 
     * Logs the number of rows deleted.
     * <p>
     * Case sensitivity depends on how the underlying database handles =.
     * 
     * @param name Value in the "name" to identify row(s) to be deleted.
     * @param language Value in the "language" to identify row(s) to be deleted.
     * @throws SQLException Thrown if SQL update fails.
     * 
     */
    private void deleteJSONWiringFromDatabase(String name, String language) throws SQLException {
        String sqlQuery;
        sqlQuery = "delete from wirings where name = \"" + name + "\" and language = \"" + language + "\"";            
        System.out.println("Running: " +  sqlQuery);
        int count = executeUpdate(sqlQuery);
        System.out.println(count + " wirings deleted");
    }

    private void deleteJSONWiringFromFileSystem(String encodedName) {

        File jsonFile = new File(jsonWiringsDir, encodedName + ".json");
        File jsonBackupFile = new File(jsonWiringsDir, encodedName + ".json.bkp");
        System.out.println("Deleting JSON wiring " + encodedName + " from " + jsonFile.getAbsolutePath());
        
        if (jsonFile.exists()){
            FileUtils.deleteQuietly(jsonFile);
        }
        if (jsonBackupFile.exists()){
            FileUtils.deleteQuietly(jsonBackupFile);
        }

    }
    
}