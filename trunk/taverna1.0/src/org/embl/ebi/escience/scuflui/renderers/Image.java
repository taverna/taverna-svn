package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Image
        extends AbstractRenderer.ByPattern
{
    private Icon icon;

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
        JPanel theImagePanel = new JPanel();
        theImagePanel.add(new JLabel(theImage));
        theImagePanel.setPreferredSize(
                new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
        return theImagePanel;
    }
}
