/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.DotView;

// Utility Imports
import java.util.Iterator;

// IO Imports
import java.io.InputStream;
import java.io.OutputStream;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import java.lang.Exception;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.String;



/**
 * A view on a ScuflModel that uses a native installation
 * of the dot tool to generate a bitmap graphical representation
 * on the fly, responding to model events appropriately
 * @author Tom Oinn
 */
public class ScuflDiagram extends JComponent 
    implements ScuflModelEventListener,
	       ScuflUIComponent {
    
    private ScuflModel model;
    private DotView dot;
    private BufferedImage image = null;
    private boolean fitToWindow = false;
    
    public ScuflDiagram() {
	super();
	setBackground(Color.white);
	setOpaque(false);
	// Create a popup menu handler
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			doEvent(e);
		    }
		}
		public void mouseReleased(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			doEvent(e);
		    }
		}
		void doEvent(MouseEvent e) {
		    JPopupMenu menu = new JPopupMenu();
		    JMenuItem title = new JMenuItem("Port display");
		    title.setEnabled(false);
		    menu.add(title);
		    menu.addSeparator();
		    JMenuItem none = new JMenuItem("No ports");
		    menu.add(none);
		    none.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				ScuflDiagram.this.setPortDisplay(DotView.NONE);
			    }
			});
		    JMenuItem bound = new JMenuItem("Bound ports only");
		    menu.add(bound);
		    bound.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				ScuflDiagram.this.setPortDisplay(DotView.BOUND);
			    }
			});
		    JMenuItem all = new JMenuItem("All ports");
		    menu.add(all);
		    all.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				ScuflDiagram.this.setPortDisplay(DotView.ALL);
			    }
			});
		    // Set whether labels are shown on edges
		    JCheckBoxMenuItem types = new JCheckBoxMenuItem("Show types", ScuflDiagram.this.dot.getTypeLabelDisplay());
		    menu.add(types);
		    types.addItemListener(new ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
				    ScuflDiagram.this.setDisplayTypes(false);
				}
				else if (e.getStateChange() == ItemEvent.SELECTED) {
				    ScuflDiagram.this.setDisplayTypes(true);
				}
			    }
			});
		    // Allow the user to select scaling
		    menu.addSeparator();
		    JCheckBoxMenuItem scale = new JCheckBoxMenuItem("Fit to window",ScuflIcons.zoomIcon,fitToWindow);
		    menu.add(scale);
		    
		    scale.addItemListener(new ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
				    ScuflDiagram.this.setFitToWindow(false);
				}
				else if (e.getStateChange() == ItemEvent.SELECTED) {
				    ScuflDiagram.this.setFitToWindow(true);
				}
			    }
			});
		    

		    menu.show(ScuflDiagram.this, e.getX(), e.getY());
		}
	    });

    }

    /**
     * Set whether the image should scale to the window or be displayed
     * at its natural size with scrollbars
     */
    public void setFitToWindow(boolean fitToWindow) {
	this.fitToWindow = fitToWindow;
	repaint();
	//paintComponent(getGraphics());
    }

    /**
     * Set whether we're displaying the port types
     */
    public void setDisplayTypes(boolean displayTypes) {
	if (this.dot != null) {
	    this.dot.setTypeLabelDisplay(displayTypes);
	    updateGraphic();
	}
    }

    /**
     * Change the port display setting, the default is to show
     * bound ports, but the other options from DotView can be
     * applied as well.
     */
    public void setPortDisplay(int portDisplayPolicy) {
	if (this.dot != null) {
	    this.dot.setPortDisplay(portDisplayPolicy);
	    updateGraphic();
	}
    }

    public Dimension getMinimumSize() {
	if (this.image != null && !fitToWindow) {
	    return new Dimension(image.getWidth(), image.getHeight());
	}
	else return super.getMinimumSize();
    }
    
    public Dimension getMaximumSize() {
	return this.getMinimumSize();
    }

    public Dimension getPreferredSize() {
	return this.getMinimumSize();
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D)g;
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			      RenderingHints.VALUE_ANTIALIAS_ON);
	super.paint(g);
    }

    public void paintComponent_old(Graphics g) {
	if (this.image != null) {
	    Graphics2D g2d = (Graphics2D)g;
	    int fx = getWidth();
	    int fy = getHeight();
	    int ix = image.getWidth();
	    int iy = image.getHeight();
	    double sx = (double)fx / (double)ix;
	    double sy = (double)fy / (double)iy;
	    double scale = sx < sy ? sx : sy;
	    scale = scale > 1.0 ? 1.0 : scale;
	    AffineTransform tx = new AffineTransform();
	    if (scale != 1.0) {
		tx.scale(scale, scale);
	    }
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			      RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.drawImage(image, tx, null);
	}
    }

    int lastFrameHeight = -1;
    int lastFrameWidth = -1;
    int lastImageHeight = -1;
    int lastImageWidth = -1;
    double lastScaleFactor = 0.0;
    java.awt.Image rescaledImage = null;
    public void paintComponent(Graphics g) {
	if (this.image != null) {
	    if (fitToWindow == false) {
		g.drawImage( image, 0, 0, image.getWidth(), image.getHeight(), null );
	    }
	    else {
		if (getWidth() == lastFrameWidth && 
		    getHeight() == lastFrameHeight && 
		    image.getWidth() == lastImageWidth && 
		    image.getHeight() == lastImageHeight &&
		    rescaledImage != null) {
		    // Repaint the previously scaled image, no need to resize it
		    g.drawImage(rescaledImage, 0, 0, null);
		}
		else {
		    double imageWidth = (double)(image.getWidth());
		    double imageHeight = (double)(image.getHeight());
		    double frameWidth = (double)getWidth();
		    double frameHeight = (double)getHeight();
		    double xscale = frameWidth / imageWidth;
		    double yscale = frameHeight / imageHeight;
		    // Get the smaller scale factor
		    double scale = xscale < yscale ? xscale : yscale;
		    lastFrameHeight = getHeight();
		    lastFrameWidth = getWidth();
		    lastImageHeight = image.getHeight();
		    lastImageWidth = image.getWidth();
		    lastScaleFactor = scale;
		    
		    // If the scale factor is greater than one then set it to one and draw the image normally
		    if (scale > 1.0) {
			scale = 1.0;
			g.drawImage( image, 0, 0, null );
		    }
		    // Otherwise regenerate the scaled image and show it.
		    else {
			if (rescaledImage != null) {
			    rescaledImage.flush();
			}
			//System.out.print("Creating new scaled instance");
			rescaledImage = this.image.getScaledInstance((int)(imageWidth * scale), 
								     (int)(imageHeight * scale),
								     java.awt.Image.SCALE_SMOOTH);
			g.drawImage( rescaledImage, 0, 0, null);
		    }
		    lastFrameHeight = getHeight();
		    lastFrameWidth = getWidth();
		    lastImageHeight = image.getHeight();
		    lastImageWidth = image.getWidth();
		    lastScaleFactor = scale;
		}
	    }
	}
    }

    public void attachToModel(ScuflModel model) {
	if (this.model == null) {
	    this.dot = new DotView(model);
	    this.dot.setPortDisplay(DotView.BOUND);
	    model.addListener(this);
	    updateGraphic();
	}
    }

    public void detachFromModel() {
	if (this.model != null) {
	    model.removeListener(this);
	    model.removeListener(dot);
	    this.dot = null;
	    this.model = null;
	    if (this.image!=null) {
		image.flush();
	    }
	    if (this.rescaledImage!=null) {
		rescaledImage.flush();
	    }
	    this.rescaledImage = null;
	    this.image = null;
	    repaint();
	}
    }

    private void updateGraphic2() {
	receiveModelEvent(null);
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
	    // Do nothing
	    // throw new RuntimeException(e);
	}
    }

    private int updateStatus = 0;
    public void receiveModelEvent(ScuflModelEvent event) {
	if (updateStatus == 0) {
	    updateStatus = 1;
	    while (updateStatus != 0) {
		updateGraphic();
		if (updateStatus == 2) {
		    updateStatus = 1;
		}
		else {
		    updateStatus = 0;
		}
	    }
	}
	else {
	    updateStatus = 2;
	}
    }

    /**
     * A name for this component
     */
    public String getName() {
	return "Scufl Diagram";
    }

}
