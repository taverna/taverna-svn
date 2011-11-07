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
import org.json.JSONObject;

public class ShowWireit extends WireitSQLBase {

    public ShowWireit() throws ServletException{
        super();
    }
        
    private void printJson(int id) throws SQLException, JSONException{
        String sqlStr = "select * from wirings where id = " + id;
        System.out.println("running: " + sqlStr);
        ResultSet rset = executeQuery(sqlStr);  // Send the query to the server
        while(rset.next()) {
            System.out.print("id: " + rset.getInt("id"));
            System.out.print("name: " + rset.getString("name"));
            System.out.print("language: " + rset.getString("language"));
            String working = URLDecoder.decode(rset.getString("working"));
            JSONObject json = new JSONObject(working);
            System.out.print("working: " + json.toString(5));
        }
        closeResultSet(rset);
    }

    public static void main(String[] args) throws ServletException, SQLException, JSONException {
        ShowWireit tester = new ShowWireit();
        tester.printJson(28);
    }

}
