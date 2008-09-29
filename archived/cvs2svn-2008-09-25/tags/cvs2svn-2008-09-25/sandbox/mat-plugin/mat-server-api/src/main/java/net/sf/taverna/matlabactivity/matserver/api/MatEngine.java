package net.sf.taverna.matlabactivity.matserver.api;

import java.util.Map;

/**
 * An interface that abstracts Matlab interfacing funtionality 
 * offered by MatServer.
 * 
 * @author petarj
 */
public interface MatEngine {

    public MatArray getVar(String name);

    public void setVar(String name, MatArray value);

    public Map<String, MatArray> getVars();

    public void setVars(Map<String, MatArray> vars);

    public Map<String, MatArray> getOutputVars();

    public void setOutputNames(String[] names);

    public void execute(String script);

    public void clearVars();
}
