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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.DotView;

/**
 * A view on a ScuflModel that uses a native installation of the dot tool to
 * generate a bitmap graphical representation on the fly, responding to model
 * events appropriately
 * 
 * @author Tom Oinn
 */
public class ScuflDiagram extends JComponent implements
		ScuflModelEventListener, ScuflUIComponent {

	private ScuflModel model;

	private DotView dot;

	private BufferedImage image = null;

	private boolean fitToWindow = false;

	private Timer updateTimer = null;

	private boolean listenToMouse = true;

	public String getDot() {
		return this.dot.getDot();
	}

	public DotView getDotView() {
		return this.dot;
	}

	public javax.swing.ImageIcon getIcon() {
		return ScuflIcons.windowDiagram;
	}

	public ScuflDiagram() {
		super();
		setBackground(Color.white);
		setOpaque(false);
	}

	/**
	 * Set whether the image should scale to the window or be displayed at its
	 * natural size with scrollbars
	 */
	public void setFitToWindow(boolean fitToWindow) {
		this.fitToWindow = fitToWindow;
		if (fitToWindow == false) {
			rescaledImage = null;
		}
		repaint();
		// paintComponent(getGraphics());
	}

	public boolean getFitToWindow() {
		return this.fitToWindow;
	}

	public void clearCachedImage() {
		rescaledImage = null;
	}

	public Dimension getMinimumSize() {
		if (this.image != null && !fitToWindow) {
			return new Dimension(image.getWidth(), image.getHeight());
		} else
			return super.getMinimumSize();
	}

	public Dimension getMaximumSize() {
		if (this.image != null && !fitToWindow) {
			return new Dimension(image.getWidth(), image.getHeight());
		} else
			return super.getMaximumSize();
	}

	public Dimension getPreferredSize() {
		if (this.image != null && !fitToWindow) {
			return new Dimension(image.getWidth(), image.getHeight());
		} else
			return super.getPreferredSize();
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}

	public void paintComponent_old(Graphics g) {
		if (this.image != null) {
			Graphics2D g2d = (Graphics2D) g;
			int fx = getWidth();
			int fy = getHeight();
			int ix = image.getWidth();
			int iy = image.getHeight();
			double sx = (double) fx / (double) ix;
			double sy = (double) fy / (double) iy;
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
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
						null);
			} else {
				if (getWidth() == lastFrameWidth
						&& getHeight() == lastFrameHeight
						&& image.getWidth() == lastImageWidth
						&& image.getHeight() == lastImageHeight
						&& rescaledImage != null) {
					// Repaint the previously scaled image, no need to resize it
					g.drawImage(rescaledImage, 0, 0, null);
				} else {
					double imageWidth = (double) (image.getWidth());
					double imageHeight = (double) (image.getHeight());
					double frameWidth = (double) getWidth();
					double frameHeight = (double) getHeight();
					double xscale = frameWidth / imageWidth;
					double yscale = frameHeight / imageHeight;
					// Get the smaller scale factor
					double scale = xscale < yscale ? xscale : yscale;
					lastFrameHeight = getHeight();
					lastFrameWidth = getWidth();
					lastImageHeight = image.getHeight();
					lastImageWidth = image.getWidth();
					lastScaleFactor = scale;

					// If the scale factor is greater than one then set it to
					// one and draw the image normally
					if (scale > 1.0) {
						scale = 1.0;
						g.drawImage(image, 0, 0, null);
					}
					// Otherwise regenerate the scaled image and show it.
					else {
						if (rescaledImage != null) {
							rescaledImage.flush();
						}
						// System.out.print("Creating new scaled instance");
						rescaledImage = this.image.getScaledInstance(
								(int) (imageWidth * scale),
								(int) (imageHeight * scale),
								java.awt.Image.SCALE_SMOOTH);
						g.drawImage(rescaledImage, 0, 0, null);
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
			updateTimer = new Timer();
			updateTimer.schedule(new UpdateTimer(), (long) 0, (long) 2000);
			// updateGraphic();
		}
	}

	public void detachFromModel() {
		if (this.model != null) {
			model.removeListener(this);
			model.removeListener(dot);
			this.dot = null;
			this.model = null;
			if (this.image != null) {
				image.flush();
			}
			if (this.rescaledImage != null) {
				rescaledImage.flush();
			}
			this.rescaledImage = null;
			this.image = null;
			repaint();
			updateTimer.cancel();
		}
	}

	private void updateGraphic2() {
		receiveModelEvent(null);
	}

	private boolean triedImageFormatFix = false;

	void updateGraphic() {
		try {
			String imageSuffix = System.getProperty(
					"taverna.scufldiagram.imagetype", "png");
			String dotText = this.dot.getDot();
			String dotLocation = System.getProperty("taverna.dotlocation");
			if (dotLocation == null) {
				dotLocation = "dot";
			}
			Process dotProcess = Runtime.getRuntime().exec(
					new String[] { dotLocation, "-T" + imageSuffix });
			OutputStream out = new BufferedOutputStream(dotProcess
					.getOutputStream());
			out.write(dotText.getBytes());
			out.flush();
			out.close();
			InputStream in = new BufferedInputStream(dotProcess
					.getInputStream());
			// Wait for the process to complete
			// dotProcess.waitFor();
			ImageInputStream iis = ImageIO.createImageInputStream(in);
			// String suffix = "png";
			Iterator readers = ImageIO.getImageReadersBySuffix(imageSuffix);
			ImageReader imageReader = (ImageReader) readers.next();
			imageReader.setInput(iis, false);
			this.image = imageReader.read(0);
			in.close();
			rescaledImage = null;
			doLayout();
			repaint();
		} catch (Exception ex) {
			if (ex instanceof ArrayIndexOutOfBoundsException
					&& !triedImageFormatFix) {
				// Catch these and craftily reset the system property
				// which defines the image type, then re-call the method
				triedImageFormatFix = true;
				System.setProperty("taverna.scufldiagram.imagetype", "gif");
				updateGraphic();
				return;
			}
			JOptionPane.showMessageDialog(ScuflDiagram.this, ex.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
			// Do nothing
			// throw new RuntimeException(e);
		}
	}

	public void receiveModelEvent(ScuflModelEvent event) {
		graphicValid = false;
	}

	boolean graphicValid = false;

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

	/**
	 * A name for this component
	 */
	public String getName() {
		return "Scufl Diagram";
	}

}
