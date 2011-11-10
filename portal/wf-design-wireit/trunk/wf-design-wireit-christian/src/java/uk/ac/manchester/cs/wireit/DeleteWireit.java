package uk.ac.manchester.cs.wireit;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Deletes a Wiring from the SQL database.
 * 
 * @author Christian
 */
public class DeleteWireit extends WireitSQLBase {
    
    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    public DeleteWireit() throws ServletException{
        super();
    }

    /**
     * Deletes a Wiring from the SQL database.
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
        String encodeName = URLEncoder.encode(name);
        String language = request.getParameter("language");
        try {
            deleteWorking(encodeName, language);
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
    private void deleteWorking(String name, String language) throws SQLException {
        String sqlQuery;
        sqlQuery = "delete from wirings where name = \"" + name + "\" and language = \"" + language + "\"";            
        System.out.println("Running: " +  sqlQuery);
        int count = executeUpdate(sqlQuery);
        System.out.println(count + " wirings deleted");
    }
    
}
