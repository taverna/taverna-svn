package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextRtf
        implements MimeTypeRendererSPI
{
    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/rtf.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        return new JEditorPane(
                "text/html", (String) userObject);
    }

    public String getName()
    {
        return "RTF";
    }
}
