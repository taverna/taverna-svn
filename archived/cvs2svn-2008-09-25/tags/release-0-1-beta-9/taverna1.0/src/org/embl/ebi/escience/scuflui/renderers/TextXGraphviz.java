package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;

// Utility Imports
import java.util.Iterator;

// IO Imports
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.awt.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TextXGraphviz
        extends AbstractRenderer.ByPattern
{
    public TextXGraphviz()
    {
        super("X-Graphviz",
              new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png")),
              Pattern.compile(".*text/x-graphviz.*"));
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
            throws RendererException
    {
        String dotText = (String) dataThing.getDataObject();
        try {
            Process dotProcess = Runtime.getRuntime().exec("dot -Tpng");
            OutputStream out = dotProcess.getOutputStream();
            out.write(dotText.getBytes());
            out.flush();
            out.close();
            InputStream in = dotProcess.getInputStream();
            ImageInputStream iis = ImageIO.createImageInputStream(in);
            String suffix = "png";
            Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
            ImageReader imageReader = (ImageReader) readers.next();
            imageReader.setInput(iis, false);
            ImageIcon theImage = new ImageIcon(imageReader.read(0));
            JPanel theImagePanel = new JPanel();
            theImagePanel.add(new JLabel(theImage));
            theImagePanel.setPreferredSize(
                    new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
            return theImagePanel;
        } catch (IOException ioe) {
            throw new RendererException("Could not render dot text", ioe);
        }
    }
 }
