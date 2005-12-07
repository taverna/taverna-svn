package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import java.util.regex.Pattern;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import java.lang.ClassLoader;
import java.lang.Object;
import java.lang.String;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Text
        extends AbstractRenderer.ByPattern
{
    public Text()
    {
        super("Text",
              new ImageIcon(Text.class.getClassLoader().getResource(
                      "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/.*"));
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
        JTextArea theTextArea = new JTextArea();
        theTextArea.setText((String) dataThing.getDataObject());
        theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return theTextArea;
    }
}
