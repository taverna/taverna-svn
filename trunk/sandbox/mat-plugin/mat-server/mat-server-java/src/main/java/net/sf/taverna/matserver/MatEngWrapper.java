package net.sf.taverna.matserver;

/**
 * Java wrapper around native Matlab Engine library.
 * @author petarj
 */
public class MatEngWrapper
{

    /** 
     * Executes script in Matlab envirnoment.
     * @param script
     *          the String to be executed.
     */
    public native void engEvalString(String script);

    /**
     * Opens Matlab engine session.
     * @param args
     *          Startup arguments.
     */
    public native void engOpen(String args);

    /**
     * Closes Matlab engine session
     */
    public native void engClose();

    /**
     * gets a variable from Matlab environment.
     * @param id
     *          Name of the variable.
     * @return 
     *          MatArray 
     */
    public native MatArray getVariable(String id);

    /**
     * sets a variable in Matlab environment
     * @param id
     *          Name of the variable.
     * @param value
     *          MatArray representing the value of the var.
     */
    public native void putVariable(String id, MatArray value);

    /**
     * Sets output buffer for accepting console output from Matlab environment.
     */
    public native void outputBuffer();

    public native void openSingleUse();

    public native boolean getVisible();

    public native void setVisible(boolean visible);
}
