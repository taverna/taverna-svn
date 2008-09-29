package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * View a URL as a clickable HTML URL.
 *
 * @author Matthew Pocock
 */
public class TextTavernaWebUrl
        extends AbstractRenderer.ByPattern
{
    public TextTavernaWebUrl()
    {
        super("Web URL",
              new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/x-taverna-web-url.*"));
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
        Object dataObject = dataThing.getDataObject();
        try {
            JEditorPane jep = new JEditorPane();
            String url = dataObject.toString();
            jep.setContentType("text/html");
            jep.setText("<a href=\"" + url + "\">" + url + "</a>");
            return jep;
        } catch (Exception ex) {
            JTextArea theTextArea = new JTextArea();
            theTextArea.setText((String) dataObject);
            theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            return theTextArea;
        }
    }
 }
