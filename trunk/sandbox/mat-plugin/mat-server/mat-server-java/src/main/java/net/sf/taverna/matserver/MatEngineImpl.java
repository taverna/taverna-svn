package net.sf.taverna.matserver;

import net.sf.taverna.matlabactivity.matserver.api.MatEngine;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of MatEngine interface that relays on Matlab Engine 
 * library.
 * 
 * @author petarj
 */
public class MatEngineImpl implements MatEngine {

    private Map<String, MatArray> vars;
    private Map<String, MatArray> outputs;
    private String[] outputNames;

    public MatEngineImpl() {
        vars = new HashMap<String, MatArray>();
        outputs = new HashMap<String, MatArray>();
    }

    public MatArray getVar(String name) {
        return vars.get(name);
    }

    public void setVar(String name, MatArray value) {
        vars.put(name, value);
    }

    public Map<String, MatArray> getVars() {
        return vars;
    }

    public void setVars(Map<String, MatArray> vars) {
        this.vars = vars;
    }

    private int getVarCount() {
        return vars.size();
    }

    String[] getVarNames() {
        return vars.keySet().toArray(new String[0]);
    }
    //XXX will this be neccessary?
    MatArray[] getVarValues() {
        return vars.values().toArray(new MatArray[0]);
    }

    public Map<String, MatArray> getOutputVars() {
        System.err.println("Engine: outputs:"+outputs.toString());
        return outputs;
    }

    public void setOutputNames(String[] names) {
        outputNames = names;
    }

    public String[] getOutputNames() {
        return outputNames;
    }

    private void setOutputVar(String name, MatArray value) {
        outputs.put(name, value);
    }

    public void clearVars() {
        vars.clear();
        outputs.clear();
        outputNames = null;
    }
    
    public native void execute(String script);

    private static native void initIDs();
    

    static {
        System.loadLibrary("MatEngineImpl");
        initIDs();
    }
}
