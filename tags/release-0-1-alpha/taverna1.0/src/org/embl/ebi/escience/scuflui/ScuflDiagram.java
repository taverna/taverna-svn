/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.DotView;

// Utility Imports
import java.util.Iterator;

// IO Imports
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.Exception;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.String;
import java.lang.System;



/**
 * A view on a ScuflModel that uses a native installation
 * of the dot tool to generate a bitmap graphical representation
 * on the fly, responding to model events appropriately
 */
public class ScuflDiagram extends JPanel 
    implements ScuflModelEventListener {
    
    private ScuflModel model;
    private DotView dot;
    private BufferedImage image = null;
    
    public ScuflDiagram() {
	super();
	setOpaque(false);
    }

    public Dimension getMinimumSize() {
	if (this.image != null) {
	    return new Dimension(image.getWidth(), image.getHeight());
	}
	else return new Dimension(200,200);
    }
    
    public Dimension getMaximumSize() {
	return this.getMinimumSize();
    }

    public Dimension getPreferedSize() {
	return this.getMinimumSize();
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D)g;
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			      RenderingHints.VALUE_ANTIALIAS_ON);
	super.paint(g);
    }

    public void paintComponent(Graphics g) {
	if (this.image != null) {
	    g.drawImage( image, 0, 0, image.getWidth(), image.getHeight(), null );
	}
    }

    public void bindToModel(ScuflModel model) {
	if (this.model == null) {
	    this.dot = new DotView(model);
	    model.addListener(this);
	    updateGraphic();
	}
    }

    public void unbindFromModel() {
	if (this.model != null) {
	    model.removeListener(this);
	    model.removeListener(dot);
	    this.dot = null;
	    this.model = null;
	    this.image = null;
	    repaint();
	}
    }

    private void updateGraphic() {
	try {
	    String dotText = this.dot.getDot();
	    Process dotProcess = Runtime.getRuntime().exec("dot -Tpng");
	    OutputStream out = dotProcess.getOutputStream();
	    out.write(dotText.getBytes());
	    InputStream in = dotProcess.getInputStream();
	    ImageInputStream iis = ImageIO.createImageInputStream(in);
	    String suffix = "png";
	    Iterator readers = ImageIO.getImageReadersBySuffix( suffix );
	    ImageReader imageReader = (ImageReader)readers.next();
	    imageReader.setInput(iis, false);
	    this.image = imageReader.read(0);
	    repaint();
	}
	catch (Exception e) {
	    System.out.println("Exception! "+e.getMessage());
	}
    }

    private int updateStatus = 0;
    public void receiveModelEvent(ScuflModelEvent event) {
	if (updateStatus == 0) {
	    updateStatus = 1;
	    while (updateStatus != 0) {
		updateStatus = 0;
		updateGraphic();
		if (updateStatus == 2) {
		    updateStatus = 1;
		}
	    }
	}
	else {
	    updateStatus = 2;
	}
    }

}
