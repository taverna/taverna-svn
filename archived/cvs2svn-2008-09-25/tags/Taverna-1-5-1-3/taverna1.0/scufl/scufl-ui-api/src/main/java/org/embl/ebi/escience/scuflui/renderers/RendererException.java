package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.scufl.ScuflException;

/**
 * Exception for when renderer-related functions go wrong. All renderer methods that fail
 * in a non-fatal way should wrap whatever exception they would raise in one of these.
 *
 * @author Matthew Pocock
 */
public class RendererException extends ScuflException
{
  public RendererException()
  {
  }

  public RendererException(String message)
  {
    super(message);
  }

  public RendererException(Throwable cause)
  {
    super(cause);
  }

  public RendererException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
