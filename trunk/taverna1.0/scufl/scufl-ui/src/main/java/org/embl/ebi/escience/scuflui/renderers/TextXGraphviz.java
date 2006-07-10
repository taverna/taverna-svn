package org.embl.ebi.escience.scuflui.renderers;

import java.awt.Dimension;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.ScuflSVGDiagram;
import org.w3c.dom.svg.SVGDocument;

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
