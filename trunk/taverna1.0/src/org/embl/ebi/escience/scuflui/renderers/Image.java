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
    private Icon icon;

    public Image() {
        icon = new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/image.png"));
    }
    
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

    public Icon getIcon(Object userObject, String mimetypes)
    {
        return icon;
    }
}
