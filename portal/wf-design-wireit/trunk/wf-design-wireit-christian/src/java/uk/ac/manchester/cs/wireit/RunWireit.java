package uk.ac.manchester.cs.wireit;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.manchester.cs.wireit.module.Resolver;
import uk.ac.manchester.cs.wireit.module.WireItRunException;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;

public class RunWireit extends WireitSQLBase {
    
    public final String TAVERNA_CMD_HOME_PARAMETER = "TAVERNA_CMD_HOME";
    private static String tavernaHome;
    
    public RunWireit() throws ServletException{
        super();
    }
 
    public void init(){
        tavernaHome = getServletContext().getInitParameter(TAVERNA_CMD_HOME_PARAMETER);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println();
        System.out.println((new Date()) + "in runWireit.doPost");
        Resolver resolver = new Resolver(request, getServletContext());
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
            jsonReply = doRun(jsonInput, outputBuilder, request, resolver);
            addRunResult(jsonReply, outputBuilder);
            String output = getOutput(parameters.get("name"), jsonReply, parameters.get("language"));
            out.println(output);
        } catch (Exception ex) {
            addRunFailed(jsonInput, ex, outputBuilder);
            String output = getOutput(parameters.get("name"), jsonInput, parameters.get("language"));
            out.println(output);
        }        
    }
  
    public static String getTavernaHome(){
        return tavernaHome;
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
        String message;
        if (ex.getMessage() != null  && !ex.getMessage().isEmpty()){
            message = ex.getMessage();
        } else {
            message = ex.getClass().getName();
        }
        outputBuilder.append(message);
        try {
            JSONObject properties = main.getJSONObject("properties"); 
            properties.put("status", "Pipe Failed");
            properties.put("details",outputBuilder.toString());
            properties.put("error", message);
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

    private JSONObject doRun(JSONObject jsonInput, StringBuilder outputBuilder, HttpServletRequest request, 
            Resolver resolver) 
            throws WireItRunException, JSONException, TavernaException, IOException{
        Wiring wiring = new Wiring(jsonInput, resolver);
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
