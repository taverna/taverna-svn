package org.embl.ebi.escience.scuflui;


import org.embl.ebi.escience.scufl.ScuflException;

public class InputsNotMatchingException extends ScuflException {

    public InputsNotMatchingException() {
        // default
    }

    public InputsNotMatchingException(String message) {
        super(message);
    }

    public InputsNotMatchingException(Throwable cause) {
        super(cause);
    }

    public InputsNotMatchingException(String message, Throwable cause) {
        super(message, cause);
    }

}
