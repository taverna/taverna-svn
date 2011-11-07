package uk.ac.manchester.cs.wireit;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.module.WireItRunException;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;

public class RunWireit extends WireitSQLBase {

    public final String TAVERNA_CMD_HOME_PARAMETER = "TAVERNA_CMD_HOME";
    public static String TAVERNA_CMD_HOME;   
    
    public static String ABSOLUTE_ROOT_FILE_PATH;
    public static String ABSOLUTE_ROOT_URL;
    
    public RunWireit() throws ServletException{
        super();
    }
 
    @Override
    public void init(){
        // Absolute path to Taverna Command Line Tool dir
        TAVERNA_CMD_HOME = getServletContext().getInitParameter(TAVERNA_CMD_HOME_PARAMETER);
        
        // Absolute path to the app's root directory (where workflows can be found, outputs will be saved, etc.)
        ABSOLUTE_ROOT_FILE_PATH = getServletContext().getRealPath("/");
        System.out.println("Absolute path to app root: " + ABSOLUTE_ROOT_FILE_PATH);
                
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        ABSOLUTE_ROOT_URL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        System.out.println("Relative path to app root: " + ABSOLUTE_ROOT_URL);

        System.out.println();
        System.out.println((new Date()) + "in runWireit.doPost");
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("Run posted at ");
        outputBuilder.append(new Date());
        outputBuilder.append("\n");
        String input = readRequestBody(request);
        HashMap<String, String> parameters = convertBody(input);
        JSONObject jsonInput = getInputJson(parameters);

        // Set the MIME type for the response message
        response.setContentType("text/x-json;charset=UTF-8");  
        // Get a output writer to write the response message into the network socket
        PrintWriter out = response.getWriter();
        JSONObject jsonReply;
        try {
            jsonReply = doRun(jsonInput, request.getRequestURL(), outputBuilder);
            addRunResult(jsonReply, outputBuilder);
            String output = getOutput(parameters.get("name"), jsonReply, parameters.get("language"));
            out.println(output);
        } catch (Exception ex) {
            addRunFailed(jsonInput, ex, outputBuilder);
            String output = getOutput(parameters.get("name"), jsonInput, parameters.get("language"));
            out.println(output);
        }        
    }
  
    private JSONObject getInputJson(HashMap<String, String> parameters) throws IOException, ServletException{
        String workingString = parameters.get("working");
        JSONObject jsonInput;
        try {
            jsonInput = new JSONObject(workingString);
            System.out.println(jsonInput.toString(4));     
        } catch (Exception ex) {
            System.err.println("Error reading input json");
            ex.printStackTrace();
            throw new ServletException(ex);
        }        
        return jsonInput;
    }
    
    private void addRunResult(JSONObject jsonReply, StringBuilder outputBuilder) throws JSONException {
        JSONObject properties = jsonReply.getJSONObject("properties"); 
        System.out.println(jsonReply.toString(4));
        properties.put("status", "Pipe run");
        outputBuilder.append("Run finished at ");
        outputBuilder.append(new Date());
        outputBuilder.append("\n");
        properties.put("details",outputBuilder.toString());
        properties.remove("error");
    }
    
    private void addRunFailed(JSONObject main, Exception ex, StringBuilder outputBuilder) throws ServletException{
        System.err.println("Error running pipe");
        ex.printStackTrace();        
        outputBuilder.append(ex.getMessage());
        try {
            JSONObject properties = main.getJSONObject("properties"); 
            properties.put("status", "Pipe Failed");
            properties.put("details",outputBuilder.toString());
            properties.put("error", ex.getMessage());
        } catch (JSONException newEx) {
            System.err.println("Error writing error to json");
            newEx.printStackTrace();
            throw new ServletException(newEx);
        }
    }

    private String getOutput(String name, JSONObject working, String language){
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\":\"0\",\n");
        builder.append("\"name\":\"");
        builder.append(name);
        builder.append("\",\n");
        String workingSt = URLEncoder.encode(working.toString());
        workingSt = workingSt.replace("\"","\\\"");
        builder.append("\"working\":\"");
        builder.append(workingSt);
        builder.append("\",\n");
        builder.append("\"language\":\"");
        builder.append(language);
        builder.append("\"}");
        return builder.toString();
    }
    
    private HashMap<String, String> convertBody(String input) {
        StringTokenizer tokens = new StringTokenizer(input, "&");
        HashMap<String, String> parameters = new HashMap<String, String>();
        while (tokens.hasMoreElements()){
            String token = tokens.nextToken();
            String key = token.substring(0,token.indexOf("="));
            String encoded = token.substring(token.indexOf("=")+1,token.length());
            String decoded = URLDecoder.decode(encoded);
            parameters.put(key, decoded);
        }
        return parameters;
    }

    private JSONObject doRun(JSONObject jsonInput, StringBuffer URL, StringBuilder outputBuilder) 
            throws WireItRunException, JSONException, TavernaException, IOException{
        Wiring wiring = new Wiring(jsonInput, URL);
        outputBuilder.append("Pipe loaded at ");
        outputBuilder.append(new Date());
        outputBuilder.append("\n");
        wiring.run(outputBuilder);
        return wiring.getJsonObject();
        //JSONArray jsonModules = jsonInput.getJSONArray("modules");
        //JSONObject commentBox = (JSONObject)jsonModules.get(0);
        //JSONObject value = commentBox.getJSONObject("value");
        //value.put("comment", "Ran sucessfullly");
        //return jsonInput;
    }

}
