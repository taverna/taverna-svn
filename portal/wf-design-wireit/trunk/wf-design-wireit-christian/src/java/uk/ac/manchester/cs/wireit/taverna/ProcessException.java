package uk.ac.manchester.cs.wireit.taverna;

/**
 * Thrown if there is a problem with the process.
 * For example if a waitFor was called before a process was started.
 * 
 * @author Christian
 */
public class ProcessException extends Exception {

     /**
     * Constructs an instance of <code>ProcessException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ProcessException(String msg) {
        super(msg);
    }
}
