package net.sf.taverna.portal.wireit;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This is a base class for the various wireit classes.
 * Adds the abilty to open a connection to the mysql server and keep it open.
 * <p>
 * The sql connection is closed after every call to avoid SQL connection timeout issues.
 * <p>
 * This is a protype and has some ugly things in it like hard coded user name and password.
 * @author Christian
 */
public class WireitSQLBase extends HttpServlet{
    
    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    WireitSQLBase() throws ServletException{
        try {
            Class.forName("com.mysql.jdbc.Driver");         // for MySQL
        } catch (ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
    
    /**
     * Executes an SQL query.
     * <p>
     * closeResultSet should be called once data has been extracted.
     * <p>
     * @param sqlStr Quesy as a String
     * @return ResultSet from which values can be extracted.
     * 
     * @throws SQLException 
     */
    ResultSet executeQuery(String sqlStr) throws SQLException{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wireit", "wireit", "taverna");
            stmt = conn.createStatement();
            return stmt.executeQuery(sqlStr);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
            throw ex;
        }
    }
            
    /**
     * Closes the resultSet and the Connection and Statment used to create it.
     * @param rset ResultSet no longer required.
     */
    void closeResultSet(ResultSet rset){
        try {
            Statement stmt = rset.getStatement();
            Connection conn = stmt.getConnection();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            //Ok closed failed no need to kill operatation.
            ex.printStackTrace();
        }    
    }
    
    /**
     * Executes an update quest and closes the connections.
     * 
     * @param sqlStr Quesy as a String
     * @return number of rows changed.
     * @throws SQLException 
     */
    int executeUpdate(String sqlStr) throws SQLException{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wireit", "wireit", "taverna");
            stmt = conn.createStatement();
            return stmt.executeUpdate(sqlStr);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }      
    }

    /**
     * Reads the data from the request body
     * @see http://java.sun.com/developer/onlineTraining/Programming/BasicJava1/servlet.html
     * @param request HTTP call
     * @return The body of the call.
     */
     String readRequestBody(HttpServletRequest request) throws IOException{
        StringBuilder json = new StringBuilder();
        String line = null;
        BufferedReader reader = request.getReader();
        while((line=reader.readLine()) != null ){
            json.append(line);
        }
        return json.toString();
    }

}
