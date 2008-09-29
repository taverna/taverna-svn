package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.util.regex.Pattern;
import java.awt.image.ImageProducer;

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
              new ImageIcon(Image.class.getClassLoader().getResource(
                "org/embl/ebi/escience/baclava/icons/image.png")),
              Pattern.compile(".*image/.*"));
    }

    protected boolean canHandle(RendererRegistry renderers,
                                Object userObject,
                                String mimeType)
    {
        return super.canHandle(renderers, userObject, mimeType) && (
                userObject instanceof byte[] ||
                userObject instanceof ImageProducer
                );
    }

    public boolean isTerminal()
    {
        return true;
    }

    public JComponent getComponent(RendererRegistry renderers,
                                   DataThing dataThing)
    {
        Object data = dataThing.getDataObject();
        if(data instanceof byte[]) {
            ImageIcon theImage = new ImageIcon((byte[]) data);
            return new JLabel(theImage);
        } else if(data instanceof ImageProducer) {
            JLabel label = new JLabel();
            java.awt.Image image = label.createImage((ImageProducer) data);
            ImageIcon icon = new ImageIcon(image);
            label.setIcon(icon);
            return label;
        }

        return null;
    }
}
