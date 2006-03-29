package org.embl.ebi.escience.scuflui.renderers;

/**
 * Exception for when renderer-related functions go wrong. All renderer methods that fail
 * in a non-fatal way should wrap whatever exception they would raise in one of these.
 *
 * @author Matthew Pocock
 */
public class RendererException extends Exception
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
