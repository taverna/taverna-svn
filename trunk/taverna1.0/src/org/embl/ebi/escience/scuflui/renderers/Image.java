package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Image
        extends AbstractRenderer.ByPattern
{
    public Image() {
        super("Image",
              new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/image.png")),
              Pattern.compile(".*image/.*"));
    }

    protected boolean canHandle(MimeTypeRendererRegistry renderers,
                                Object userObject,
                                String mimeType)
    {
        return super.canHandle(renderers, userObject, mimeType) &&
                userObject instanceof byte[];
    }

    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing)
    {
        ImageIcon theImage = new ImageIcon((byte[]) dataThing.getDataObject());
        return new JLabel(theImage);
    }
}
