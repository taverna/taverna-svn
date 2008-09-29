package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * Abstract rendering class. Inner classes refine this further.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractRenderer
        implements RendererSPI
{
  private String name;
  private Icon icon;

  public AbstractRenderer(String name)
  {
    this.name = name;
  }

  public AbstractRenderer(String name, Icon icon)
  {
    this.name = name;
    this.icon = icon;
  }

  public final Icon getIcon(RendererRegistry renderers,
                            DataThing dataThing)
  {
    return icon;
  }

  public final String getName()
  {
    return name;
  }


  /**
   * Choses to accept a DataThing based upoon its mime type.
   *
   * @author Matthew Pocock
   */
  public abstract static class ByMimeType
          extends AbstractRenderer
  {
    protected ByMimeType(String name)
    {
      super(name);
    }

    protected ByMimeType(String name, Icon icon)
    {
      super(name, icon);
    }

    public final boolean canHandle(RendererRegistry renderers,
                                   DataThing dataThing)
    {
      String[] mimeTypes = dataThing.getMetadata().getMIMETypes();
      Object userObject = dataThing.getDataObject();
      for (int i = 0; i < mimeTypes.length; i++) {
        if (canHandle(renderers, userObject, mimeTypes[i])) {
          return true;
        }
      }

      return false;
    }

    /**
     * Make a decision about each mime type presented.
     *
     * @param renderers  the sibling renderers
     * @param userObject the user data object to render
     * @param mimeType   one of the mime types associated with it
     * @return true if we can handle this object with this mime type
     */
    protected abstract boolean canHandle(RendererRegistry renderers,
                                         Object userObject,
                                         String mimeType);
  }

    /**
     * Accept based on user object type match to a particular Class
     */
    public abstract static class ByJavaClass
	extends AbstractRenderer {
	
	protected ByJavaClass(String name) {
	    super(name);
	}
	
	protected ByJavaClass(String name, Icon icon) {
	    super(name, icon);
	}
	
	public final boolean canHandle(RendererRegistry renderers,
				       DataThing dataThing) {
	    Class dataObjectClass = dataThing.getDataObject().getClass();
	    return canHandle(renderers, dataThing, dataObjectClass);
	}
	
	protected abstract boolean canHandle(RendererRegistry renderers,
					     Object userObject,
					     Class dataClass);
    }

  /**
   * Accept a DataThing based upon the mime type matching a regular expression
   * pattern.
   *
   * @author Matthew Pocock
   */
  public static abstract class ByPattern
          extends ByMimeType
  {
    private final Pattern pattern;

    public ByPattern(String name, Pattern pattern)
    {
      super(name);
      this.pattern = pattern;
    }

    public ByPattern(String name, Icon icon, Pattern pattern)
    {
      super(name, icon);
      this.pattern = pattern;
    }

    public Pattern getPattern()
    {
      return pattern;
    }

    protected boolean canHandle(RendererRegistry renderers,
                                Object userObject,
                                String mimeType)
    {
      return pattern.matcher(mimeType).matches();
    }

    /**
     * Strips out all mime types from the list that match our pattern. This is usefull if you want to pass the mime
     * types back to a registry while ensuring that you won't pull out yourself again.
     *
     * @param mimeTypes original mime types
     * @return the sub-list of these that don't match this renderer
     */
    protected String[] strip(String[] mimeTypes)
    {
      ArrayList result = new ArrayList(mimeTypes.length);
      for (int i = 0; i < mimeTypes.length; i++) {
        String mimeType = mimeTypes[i];
        if (!getPattern().matcher(mimeType).matches()) {
          result.add(mimeType);
        }
      }

      return (String[]) result.toArray(new String[result.size()]);
    }

  }

  public abstract class BySemanticType
          extends AbstractRenderer
  {
    protected BySemanticType(String name)
    {
      super(name);
    }

    protected BySemanticType(String name, Icon icon)
    {
      super(name, icon);
    }

    public final boolean canHandle(RendererRegistry renderers,
                                   DataThing dataThing)
    {
      String semanticType = dataThing.getMetadata().getSemanticType();
      Object userObject = dataThing.getDataObject();
      if (canHandle(renderers, userObject, semanticType)) {
        return true;
      }

      return false;
    }

    protected abstract boolean canHandle(RendererRegistry renderers,
                                         Object userObject,
                                         String semanticType);
  }
}
