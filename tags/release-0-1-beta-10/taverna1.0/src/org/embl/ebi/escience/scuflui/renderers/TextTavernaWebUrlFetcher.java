package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import javax.swing.*;
import java.net.URL;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Display the content of a URL.
 *
 * @author Matthew Pocock
 */
public class TextTavernaWebUrlFetcher
        extends AbstractRenderer.ByPattern
{
    public TextTavernaWebUrlFetcher()
    {
        super("Web URL Fetcher",
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
        return false;
    }

    public JComponent getComponent(RendererRegistry renderers,
                                   DataThing dataThing)
    {
        Object dataObject = dataThing.getDataObject();
        try {
            URL url = new URL((String) dataObject);
            DataThing urlThing = DataThingFactory.fetchFromURL(url);
            return renderers.getRenderer(urlThing).getComponent(
                    renderers, urlThing);
        } catch (Exception ex) {
            JTextArea theTextArea = new JTextArea();
            theTextArea.setText((String) dataObject);
            theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            return theTextArea;
        }
    }
 }
