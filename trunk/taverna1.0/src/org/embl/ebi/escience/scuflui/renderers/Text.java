package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;
import java.awt.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Text
        implements MimeTypeRendererSPI
{
    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        JTextArea theTextArea = new JTextArea();
        theTextArea.setText((String) userObject);
        theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return theTextArea;
    }

    public String getName()
    {
        return "Text";
    }
}
