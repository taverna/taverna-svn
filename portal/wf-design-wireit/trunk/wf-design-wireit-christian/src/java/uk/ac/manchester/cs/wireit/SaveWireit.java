package uk.ac.manchester.cs.wireit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//Optional can be commented out to avoid using org.json
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Receives a Wiring from WireIt and saves it to the the SQL database.
 * 
 * @author Christian
 */
public class SaveWireit extends WireitSQLBase {
    
    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    public SaveWireit() throws ServletException{
        super();
    }
 
    /**
     * Receives a Wiring from WireIt and saves it to the the SQL database.
     * <p>
     * Wiring is retrieved from the body of the request.
     * <p>
     * Wirings are received:
     * <ul>
     * <li>URLEncoded</li>
     * <li>In the format key=value&key=value&...
     * <li>keys are
     *     <ul>
     *         <li>name</li>
     *         <li>language<li>
     *         <li>working</li>
     *     </ul>
     * <li>All values are Strings
     * </ul>
     * <p>
     * It is recommeneded to save the values URLEncoded as especially working will contain many special characters
     *    such as space, quotes, brackets (curly and square), commas and semicolumns
     * 
     * @param request Body is expected to hold the request. Query and parameters are ignored.
     * @param response "{\"error\":null}" in "text/x-json;charset=UTF-8" format.
     * @throws ServletException Thrown if the data is in an unexpected format. Including.
     * <ul>
     * <li> name is missing of longer than 255 characters</li>
     * <li> language is missing of longer than 255 characters</li>
     * <li> working is missing or not parasable to json (when URLDecoded)</li>
     * <li> There was an SQLException running the update
     * <ul>
     * @throws IOException Thrown if the response can not be written.
     */ 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println();
        System.out.println((new Date()) + "in SaveWireit.doPost");
                
        try {
            String json = readRequestBody(request);
            saveWorking(json);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex);
        }
        
        response.setContentType("text/x-json;charset=UTF-8");  
        PrintWriter out = response.getWriter();
        out.println("{\"error\":null}");             
        
    }
        
     /**
      * Parses the jsonString into name, language and working and then call other method to save to sql.
      * 
      * See doPost for comments on the format.
      * Optionally working is check to make sure it is valid json.
      * @param jsonString String as recieved from request
      * @throws ServletException Unexpected inpit see doPost
      * @throws JSONException Thrown when working can not be convert to a Json object
      * @throws SQLException Thrown if sql update fails
      */
    private void saveWorking(String jsonString) throws ServletException, JSONException, SQLException{
        String name = null;
        String workingEncoded = null;
        String language = null;
        System.out.println(jsonString);
        StringTokenizer tokens = new StringTokenizer(jsonString, "&");
        while (tokens.hasMoreElements()){
            String token = tokens.nextToken();
            String key = token.substring(0,token.indexOf("="));
            if (key.equalsIgnoreCase("name")){
                name =  token.substring(token.indexOf("=")+1,token.length());
            } else if (key.equalsIgnoreCase("language")){
                language =  token.substring(token.indexOf("=")+1,token.length());
            } else if (key.equalsIgnoreCase("working")){
                workingEncoded =  token.substring(token.indexOf("=")+1,token.length());
            } else {
                throw new ServletException("Unexpected key " + key + " in body");
            }
        }
        //Check name and length exist and are not too long.
        if (name.length() > 255){
            throw new ServletException("Maximum size of name is 255");
        }
        if (language.length() > 255){
            throw new ServletException("Maximum size of language is 255");
        }
        //Create a json object as an easy check for an SQL insert attack
        String workingDecoded = URLDecoder.decode(workingEncoded);
        JSONObject jsonWorking = new JSONObject(workingDecoded);
        saveWorking(name, workingEncoded, language);
    }
       
    /**
     * Saves the name, working and language to the sql database.
     * 
     * Checks to see if the name and language pair already exist.
     * If so an update is sent.
     * If not an insert is sent
     * @param name Name of the working
     * @param workingEncoded working in URLEncoded format.
     * @param language language
     * @throws SQLException Thrown if query, update or insert failed.
     */
    private void saveWorking(String name, String workingEncoded, String language) throws SQLException {
        String sqlQuery;
        if (findWorking(name, language)){
            sqlQuery = "update wirings set working = \"" + workingEncoded + 
                    "\" where name = \"" + name + "\" and language = \"" + language + "\"";            
        } else {
            sqlQuery = "insert into wirings set name = \"" + name + "\", working = \"" + workingEncoded + 
                "\", language = \"" + language + "\"";
        }
        doSQLUpdate(sqlQuery);
    }

    /**
     * Does an sql update and logs how many records changed
     * 
     * @param sqlStr Update to run
     * @throws SQLException Thrown if update fails.
     */
    private void doSQLUpdate(String sqlStr) throws SQLException {
        System.out.println("Running: " +  sqlStr);
        int count = executeUpdate(sqlStr);
        System.out.println(count + " wirings saved");
     }
    
    /**
     * Checks to see if there is already a row with this name and language.
     * 
     * @param name Name to check
     * @param language Language to check
     * @return True if and only if there is already a row with this name and language.
     *     Case sensitivity is as the database would handle =
     * @throws SQLException Thrown if the query fails.
     */
    public boolean findWorking(String name, String language) throws SQLException { 
        String sqlStr = "select name from wirings where name = \"" + name +"\" and language = \"" + language + "\"";
        ResultSet rset = executeQuery(sqlStr);  // Send the query to the server
        int count = 0;
        while(rset.next()) {
            count++;
        }
        closeResultSet(rset);
        return (count >= 1);
   }

}
