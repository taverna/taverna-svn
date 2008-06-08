package net.sf.taverna.matserver;

/**
 *
 * @author petarj
 */
public interface MEngine
{

    public void execScript(String script);

    public MatArray getVariable(String name);

    public void setVariable(String name, MatArray var);
    //TODO: define graphics handling!!!
}
