package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextHtml
        implements MimeTypeRendererSPI
{
    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/html.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        return new JEditorPane(
                "text/html", "<pre>" + (String) userObject + "</pre>");
    }

    public String getName()
    {
        return "HTML";
    }
}
