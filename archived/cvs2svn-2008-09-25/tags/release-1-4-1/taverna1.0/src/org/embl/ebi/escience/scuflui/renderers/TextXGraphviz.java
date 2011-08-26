package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import org.embl.ebi.escience.scuflui.*;

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
import org.apache.batik.swing.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.util.*;
import org.w3c.dom.svg.*;

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
              new ImageIcon(TextXGraphviz.class.getClassLoader().getResource(
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
	    SVGDocument doc = ScuflSVGDiagram.getSVG(dotText);
	    JSVGCanvas canvas = new JSVGCanvas();
	    canvas.setSVGDocument(doc);
	    JSVGScrollPane pane = new JSVGScrollPane(canvas);
	    pane.setPreferredSize(new Dimension(0,0));
	    return pane;
	}
	catch (IOException ioe) {
            throw new RendererException("Could not render dot text", ioe);
        }
    }
 }
