/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.view.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

import org.apache.batik.swing.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.util.*;
import org.w3c.dom.svg.*;

/**
 * An SVG based version of the ScuflDiagram
 * @author Tom Oinn
 */
public class ScuflSVGDiagram extends JComponent 
    implements ScuflModelEventListener,
	       ScuflUIComponent {
    
    private ScuflModel model;
    private DotView dot;
    boolean graphicValid = false;
    JSVGCanvas svgCanvas;
    JSVGScrollPane pane;
    Timer updateTimer = null;


    public String getDot() {
	return this.dot.getDot();
    }
    
    public DotView getDotView() {
	return this.dot;
    }

    public ImageIcon getIcon() {
	return ScuflIcons.windowDiagram;
    }
    
    public ScuflSVGDiagram() {
	super();
	setBackground(Color.white);
	setOpaque(false);
	setLayout(new BorderLayout());
	svgCanvas = new JSVGCanvas();
	pane = new JSVGScrollPane(svgCanvas);
	pane.setPreferredSize(new Dimension(0,0));
	add(pane, BorderLayout.CENTER);
	JPanel statusPanel = new JPanel();
	statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
	final JLabel statusLabel = new JLabel("No document");
	statusPanel.add(Box.createRigidArea(new Dimension(5,5)));
	statusPanel.add(statusLabel);
	statusPanel.add(Box.createHorizontalGlue());
	statusPanel.setMaximumSize(new Dimension(6000,30));
	add(statusPanel, BorderLayout.SOUTH);
	svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
		public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
		    statusLabel.setText("Loading document...");
		}
		public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
		    statusLabel.setText("Document loaded.");
		}
	    });
	svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
		public void gvtBuildStarted(GVTTreeBuilderEvent e) {
		    statusLabel.setText("Build started...");
		}
		public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
		    statusLabel.setText("Build done.");
		}
	    });
	svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
		public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
		    statusLabel.setText("Rendering started...");
		}
		public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
		    statusLabel.setText("Rendering done.");
		}
	    });
    }
    
    public void attachToModel(ScuflModel model) {
	if (this.model == null) {
	    this.dot = new DotView(model);
	    this.dot.setPortDisplay(DotView.BOUND);
	    model.addListener(this);
	    graphicValid = false;
	    updateTimer = new Timer();
	    updateTimer.schedule(new UpdateTimer(), (long)0, (long)2000);	    
	}
    }
    
    public void detachFromModel() {
	if (this.model != null) {
	    model.removeListener(this);
	    model.removeListener(dot);
	    this.dot = null;
	    this.model = null;
	    repaint();
	    updateTimer.cancel();
	}
    }

    class UpdateTimer extends TimerTask {
	public UpdateTimer() {
	    super();
	}
	public void run() {
	    if (!graphicValid) {
		graphicValid = true;
		updateGraphic();
	    }
	}
    }
    
    public String getName() {
	return "Scufl SVG Diagram";
    }
    
    public void receiveModelEvent(ScuflModelEvent event) {
	graphicValid = false;
    }

    synchronized void updateGraphic() {
	try {
	    svgCanvas.setSVGDocument(getSVG(getDot()));
	    pane.revalidate();
	}
	catch (IOException ioe) {
	    JOptionPane.showMessageDialog(ScuflSVGDiagram.this,
					  ioe.getMessage(),
					  "Error!",
					  JOptionPane.ERROR_MESSAGE);
	}
	catch (Exception other) {
	    other.printStackTrace();	    
	}
    }

    static SAXSVGDocumentFactory docFactory = null;
    
    static {
	String parser = XMLResourceDescriptor.getXMLParserClassName();
	docFactory = new SAXSVGDocumentFactory(parser);
    }

    SVGDocument getSVG(String dotText) throws IOException {
	String dotLocation = System.getProperty("taverna.dotlocation");
	if (dotLocation == null) {
	    dotLocation = "dot";
	}
	System.out.println("Invoking dot...");
	Process dotProcess = Runtime.getRuntime().exec(new String[]{dotLocation,"-Tsvg"});
	StreamDevourer devourer = new StreamDevourer(dotProcess.getInputStream());
	devourer.start();
	OutputStream out = new BufferedOutputStream(dotProcess.getOutputStream());
	out.write(dotText.getBytes());
	out.flush();
	out.close();
	System.out.println(devourer.blockOnOutput());
	return docFactory.createSVGDocument("http://taverna.sf.net/diagram/generated.svg", new StringReader(devourer.toString()));
    }

    class StreamDevourer extends Thread {
	BufferedReader br;
	ByteArrayOutputStream output;
	public String toString() {
	    return output.toString();
	}
	public String blockOnOutput() {
	    try {
		this.join();
		return output.toString();
	    }
	    catch (InterruptedException ie) {
		ie.printStackTrace();
		return "Interrupted!";
	    }
	}
	public StreamDevourer(InputStream is) {
	    super();
	    this.br = new BufferedReader(new InputStreamReader(is));	    
	    this.output = new ByteArrayOutputStream();
	}
	public void run() {
	    try {
		String line = null;
		while ((line = br.readLine()) != null && line.endsWith("</svg>") == false) {
		    System.out.println(line);
		    output.write(line.getBytes());
		}
		if (line != null) {
		    output.write(line.getBytes());
		}
		br.close();
	    }
	    catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
    }

}
