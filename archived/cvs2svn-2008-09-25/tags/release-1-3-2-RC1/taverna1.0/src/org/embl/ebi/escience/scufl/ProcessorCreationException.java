/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the Processor constructor if either of the
 * arguments passed are null, or if the name is
 * the empty string.
 * @author Tom Oinn
 */
public class ProcessorCreationException extends Exception {
  public ProcessorCreationException()
  {
  }

  public ProcessorCreationException(String message)
  {
    super(message);
  }

  public ProcessorCreationException(Throwable cause)
  {
    super(cause);
  }

  public ProcessorCreationException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
