package org.embl.ebi.escience.scuflui.renderers;

import java.awt.Dimension;
import java.awt.Font;
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

import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;
import java.lang.ClassLoader;
import java.lang.Object;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.String;



/**
 *
 *
 * @author Matthew Pocock
 */
public class TextXGraphviz
        implements MimeTypeRendererSPI
{
    private Icon icon;

    public TextXGraphviz()
    {
        icon = new ImageIcon(ClassLoader.getSystemResource(
                "org/embl/ebi/escience/baclava/icons/text.png"));
    }

    public boolean canHandle(Object userObject, String mimetypes)
    {
        return mimetypes.matches(".*text/x-graphviz.*") &&
                userObject instanceof String;
    }

    public JComponent getComponent(Object userObject, String mimetypes)
    {
        try {
            String dotText = (String) userObject;
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
            theImagePanel.setPreferredSize(new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
            return theImagePanel;
        } catch (IOException ioe) {
            JTextArea theTextArea = new JTextArea();
            theTextArea.setText((String) userObject);
            theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            return theTextArea;
        }
    }

    public String getName()
    {
        return "X-Graphviz";
    }

    public Icon getIcon(Object userObject, String mimetypes)
    {
        return icon;
    }
 }
