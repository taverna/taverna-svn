package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.embl.ebi.escience.scuflui.XMLTree;
import org.embl.ebi.escience.baclava.DataThing;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextXml
        extends AbstractRenderer.ByPattern
{
    public TextXml()
    {
        super("XML",
              new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/xml.*"));
    }

    protected boolean canHandle(MimeTypeRendererRegistry renderers,
                                Object userObject,
                                String mimeType)
    {
        return super.canHandle(renderers, userObject, mimeType) &&
                userObject instanceof String;
    }

    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing)
    {
        DataThing copy = new DataThing(dataThing);
        copy.getMetadata().setMIMETypes(
                Arrays.asList(strip(dataThing.getMetadata().getMIMETypes())));

        MimeTypeRendererSPI delegate = renderers.getRenderer(copy);
        JComponent plain = delegate.getComponent(
                renderers, copy);
        try {
            XMLTree xmlTreeDisplay = new XMLTree(
                    (String) dataThing.getDataObject());
            JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              new JScrollPane(xmlTreeDisplay),
                                              plain);
            return pane2;
        } catch (Exception ex) {
            return plain;
        }
    }
 }
