package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class LabelRenderer
        implements MimeTypeRendererSPI
{
    public boolean canHandle(MimeTypeRendererRegistry renderers,
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

        return true;
    }

    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing)
    {
        Object dataObject = dataThing.getDataObject();

        if(dataObject instanceof Number
                || dataObject instanceof CharSequence)
        {
            return new JLabel(dataObject.toString());
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

    public Icon getIcon(MimeTypeRendererRegistry renderers,
                        DataThing dataThing)
    {
        return null;
    }
}
