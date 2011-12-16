package net.sf.taverna.portal.wireit;

import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Debug class used to retreive and show a single saved pipe well formatted.
 * @author Christian
 */
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
        tester.printJson(6);
    }

}
