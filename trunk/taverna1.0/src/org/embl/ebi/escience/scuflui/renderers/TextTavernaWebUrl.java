package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;
import java.net.URL;
import java.awt.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextTavernaWebUrl
        implements MimeTypeRendererSPI
{
    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/x-taverna-web-url.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        try {
            JEditorPane jep = new JEditorPane();
            jep.setPage(new URL((String) userObject));
            return jep;
        } catch (Exception ex) {
            JTextArea theTextArea = new JTextArea();
            theTextArea.setText((String) userObject);
            theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            return theTextArea;
        }
    }

    public String getName()
    {
        return "Web URL";
    }
}
