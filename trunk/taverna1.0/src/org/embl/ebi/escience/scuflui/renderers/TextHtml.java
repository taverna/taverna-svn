package org.embl.ebi.escience.scuflui.renderers;

import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.embl.ebi.escience.baclava.DataThing;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextHtml
        extends AbstractRenderer.ByPattern
{
    public TextHtml()
    {
        super("HTML",
              new ImageIcon(TextHtml.class.getClassLoader().getResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/html.*"));
    }

    protected boolean canHandle(RendererRegistry renderers,
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

    public JComponent getComponent(RendererRegistry renderers,
                                   DataThing dataThing)
    {
        return new JEditorPane(
                "text/html", "<pre>" + (String) dataThing.getDataObject() + "</pre>");
    }
 }
