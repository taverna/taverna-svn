package uk.ac.manchester.cs.wireit.module;

/**
 *
 * @author Christian
 */
public class WireItRunException extends Exception {

    /**
     * Constructs an instance of <code>WireItRunException</code> with the specified detail message and inner Exception.
     * @param msg the detail message.
     */
    public WireItRunException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>WireItRunException</code> with the specified detail message and inner Exception.
     * @param msg the detail message.
     * @param ex The Exception that was caught and wrapped.
     */
    public WireItRunException(String msg, Exception ex) {
        super(msg, ex);
        ex.printStackTrace();
    }
}
