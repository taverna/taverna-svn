package uk.ac.manchester.cs.wireit;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//Json element are optional and can be commented out.
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Reads the save WireIt workings from database and returns then to WireIt.
 * @author Christian
 */
public class ListWireit extends WireitSQLBase {

    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    public ListWireit() throws ServletException{
        super();
    }
        
    /**
     * Reads the save WireIt workings for this langauge from database and returns then to WireIt.
     * <p>
     *     See WireIt\examples\ajaxAdapter\listWirings.json for an example format.
     * <p>
     * Notable points include
     * <ul>
     * <li>Wireit sends out URLEncoded but expects URLDecoded back</li>
     * <li>Wireit expects back Json (even though that is not sent out)</li>
     * <li>All the objects are Strings so must be wrapped in quotes</li>
     * <li>The quotes within the working String but all be preceeded by a \</li>
     * </ul>
     * @param request Should have a language parameter.
     * @param response Returns the wirings in the format WireIt expects.
     *     See WireIt\examples\ajaxAdapter\listWirings.json for an example format.
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println();
        System.out.println((new Date()) + "in ListWireit.doGet");
        
        String language = request.getParameter("language");
        
        // Set the MIME type for the response message
        response.setContentType("text/x-json;charset=UTF-8");  
        // Get a output writer to write the response message into the network socket
        PrintWriter out = response.getWriter();
 
        try {
            out.println(getJsonString(language));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex);
        }
    }
       
    /**
     * Runs a select * query where language = language and returns the results as a Json String.
     * 
     * @param language Language to be selected for null for all languages
     * @return json String without spaces or line breaks.
     * @throws SQLException Thrown if the query fails
     */
    private String getJsonString(String language) throws SQLException{
        String sqlStr;
        if (language == null){
            sqlStr = "select * from wirings";
        } else {
            sqlStr = "select * from wirings where language = \"" + language + "\"";
        }
        System.out.println("running: " + sqlStr);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean notFirst = false;
        ResultSet rset = executeQuery(sqlStr);  // Send the query to the server
        while(rset.next()) {
            if (notFirst){
                builder.append(",\n");
            } else {
                notFirst = true;
            }
            appendResult(rset, builder, language);
        }
        builder.append("]");
        closeResultSet(rset);
        return builder.toString();
    }

    
    private void appendResult(ResultSet rset, StringBuilder builder, String language) throws SQLException{
                   // Print a paragraph <p>...</p> for each record
        builder.append("{\"id\":\"");
        builder.append(rset.getInt("id"));
        builder.append("\",\n");
        String name = URLDecoder.decode(rset.getString("name"));
        builder.append("\"name\":\"");
        builder.append(name);
        builder.append("\",\n");
        String working = URLDecoder.decode(rset.getString("working"));
        //Json needs quotes " to be escpaped
        working = working.replace("\"","\\\"");
        //Json needs carriage returns to be escpaed.
        working = working.replace("\\n","\\\\n");
        builder.append("\"working\":\"");
        //JSONObject workingJson = new JSONObject(working);
        //builder.append(workingJson.toString(4));
        builder.append(working);
        builder.append("\",\n");
        language = URLDecoder.decode(rset.getString("language"));
        builder.append("\"language\":\"");
        builder.append(rset.getString("language"));
        builder.append("\"}");
    }
    
    /**
     * Testing and showing method.
     * 
     * Reads the data from the database prints it out unformated. 
     * Checks it is parsabale json
     * Prints it out formated.
     * <p>
     * This method is optional. Comment out if org.json not included.
     * 
     * @param args All ignored.
     * @throws ServletException If the SQL connection could not be made.  
     *          Including if the hard coded database, user and password are not found.
     * @throws SQLException Thrown if the query fails
     * @throws JSONException Thrown if the result is not in json parsable format.
     */
    public static void main(String[] args) throws ServletException, SQLException, JSONException {
        ListWireit tester = new ListWireit();
        String input = tester.getJsonString(null);
        System.out.println(input);
        JSONArray json = new JSONArray(input);
        System.out.println(json.toString(4));
    }

}
