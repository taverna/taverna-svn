package org.embl.ebi.escience.scuflui.renderers;


import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import org.embl.ebi.escience.baclava.DataThing;

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
              new ImageIcon(TextTavernaWebUrl.class.getClassLoader().getResource(
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
