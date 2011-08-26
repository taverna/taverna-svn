package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.embl.ebi.escience.baclava.DataThing;

/**
 *
 *
 * @author Matthew Pocock
 */
public class LabelRenderer
        implements RendererSPI
{
    public boolean canHandle(RendererRegistry renderers,
                             DataThing dataThing)
    {
        String[] mimeTypes = dataThing.getMetadata().getMIMETypes();
        Object dataObject = dataThing.getDataObject();

        if(mimeTypes.length == 0) {
            if(dataObject instanceof Number
                    || dataObject instanceof CharSequence
                    || dataObject instanceof Image
                    || dataObject instanceof Icon)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isTerminal()
    {
        return true;
    }

    public JComponent getComponent(RendererRegistry renderers,
                                   DataThing dataThing)
    {
        Object dataObject = dataThing.getDataObject();

        if(dataObject instanceof Number)
        {
            return new JTextArea(dataObject.toString());
        }

        if(dataObject instanceof String)
        {
            return new JTextArea((String) dataObject);
        }

        if(dataObject instanceof CharSequence)
        {
            return new JTextArea(dataObject.toString());
        }

        if(dataObject instanceof Image) {
            return new JLabel(new ImageIcon((java.awt.Image) dataObject));
        }

        if(dataObject instanceof Icon) {
            return new JLabel((Icon) dataObject);
        }

        return null;
    }

    public String getName()
    {
        return "LabelRenderer";
    }

    public Icon getIcon(RendererRegistry renderers,
                        DataThing dataThing)
    {
        return null;
    }
}
