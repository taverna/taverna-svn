package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;
import java.awt.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Image
        implements MimeTypeRendererSPI
{
    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*image/.*") &&
                userObject instanceof byte[];
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        ImageIcon theImage = new ImageIcon((byte[]) userObject);
        JPanel theImagePanel = new JPanel();
        theImagePanel.add(new JLabel(theImage));
        theImagePanel.setPreferredSize(new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
        return theImagePanel;
    }

    public String getName()
    {
        return "Image";
    }
}
