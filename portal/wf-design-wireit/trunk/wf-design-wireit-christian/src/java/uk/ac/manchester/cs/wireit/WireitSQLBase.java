package uk.ac.manchester.cs.wireit;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This is a base class for the various wireit classes.
 * Adds the abilty to open a connection to the mysql server and keep it open.
 * 
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
     * @param request
     * @return 
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
