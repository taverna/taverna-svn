package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextRtf
        extends AbstractRenderer.ByPattern
{
    public TextRtf()
    {
        super("RTF",
              new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/rtf.*"));
    }

    protected boolean canHandle(MimeTypeRendererRegistry renderers,
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

    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing)
    {
        return new JEditorPane(
                "text/html", (String) dataThing.getDataObject());
    }
}
