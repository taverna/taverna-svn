package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.embl.ebi.escience.scuflui.XMLTree;

import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;
import org.embl.ebi.escience.scuflui.renderers.Text;
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
        implements MimeTypeRendererSPI
{
    private Icon icon;

    public TextXml()
    {
        icon = new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png"));

    }

    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/xml.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        JComponent plain = new Text().getComponent(
                userObject, mimetypes);
        try {
            XMLTree xmlTreeDisplay = new XMLTree((String) userObject);
            JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              new JScrollPane(xmlTreeDisplay),
                                              plain);
            return pane2;
        } catch (Exception ex) {
            return plain;
        }
    }

    public String getName()
    {
        return "XML";
    }

    public Icon getIcon(Object userObject, String mimetypes)
    {
        return icon;
    }
 }
