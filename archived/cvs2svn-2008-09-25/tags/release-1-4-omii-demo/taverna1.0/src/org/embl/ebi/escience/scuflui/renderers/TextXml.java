package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.embl.ebi.escience.scuflui.XMLTree;
import org.embl.ebi.escience.baclava.DataThing;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;

/**
 * Viewer to display XML as a tree.
 *
 * @author Matthew Pocock
 */
public class TextXml
        extends AbstractRenderer.ByPattern
{
    public TextXml()
    {
        super("XML",
              new ImageIcon(TextXml.class.getClassLoader().getResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/xml.*"));
    }

    protected boolean canHandle(RendererRegistry renderers,
                                Object userObject,
                                String mimeType)
    {
        return super.canHandle(renderers, userObject, mimeType) &&
                userObject instanceof String;
    }

    public boolean isTerminal()
    {
        return true;
    }

    public JComponent getComponent(RendererRegistry renderers,
                                   DataThing dataThing)
            throws RendererException
    {
        DataThing copy = new DataThing(dataThing);
        copy.getMetadata().setMIMETypes(
                Arrays.asList(strip(dataThing.getMetadata().getMIMETypes())));

        try {
            return new XMLTree((String) dataThing.getDataObject());
        } catch (Exception ex) {
          throw new RendererException(ex);
        }
    }
 }
