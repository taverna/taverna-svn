/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Implementing Scufl objects can have log level
 * information bound to them
 * @author Tom Oinn
 */
public interface LogAwareComponent {
    
    /**
     * Set the log level for this component
     */
    public void setLogLevel(int level);
    
    /**
     * Get the log level for this component
     */
    public int getLogLevel();

}
