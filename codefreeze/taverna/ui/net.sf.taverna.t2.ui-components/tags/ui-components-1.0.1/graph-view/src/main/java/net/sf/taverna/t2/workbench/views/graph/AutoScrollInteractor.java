package net.sf.taverna.t2.workbench.views.graph;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.InteractorAdapter;
import org.apache.batik.swing.gvt.JGVTComponent;

/**
 * An interactor that scrolls the canvas view if the mouse is dragged to the
 * edge of the canvas.
 * 
 * @author David Withers
 */
public class AutoScrollInteractor extends InteractorAdapter {

	/**
	 * Defines the border around the canvas in which the auto scroll will become
	 * active.
	 */
	private static final int BORDER = 25;

	/**
	 * The interval, in milliseconds, between scroll events.
	 */
	private static long SCROLL_INTERVAL = 100;

	private JSVGCanvas svgCanvas;

	private Dimension canvasSize;

	private int scrollX;
	private int scrollY;

	private int mouseX;
	private int mouseY;

	/**
	 * Component used to identify mouse events generated by this class
	 */
	private Component eventIdentifier = new Component() {
		private static final long serialVersionUID = -295542754718804222L;
	};

	private static Timer timer = new Timer("GraphAutoScrollTimer", true);

	private TimerTask task;

	/**
	 * Whether the interactor has finished.
	 */
	protected boolean finished = true;

	public AutoScrollInteractor(JSVGCanvas svgCanvas) {
		this.svgCanvas = svgCanvas;
	}

	public boolean startInteraction(InputEvent ie) {
		boolean start = false;
		int mods = ie.getModifiers();
		if (ie.getID() == MouseEvent.MOUSE_PRESSED
				&& (mods & InputEvent.BUTTON1_MASK) != 0) {
			AffineTransform transform = svgCanvas.getRenderingTransform();
			// check if we're zoomed in
			if (transform.getScaleX() > 1d || transform.getScaleY() > 1d) {
				canvasSize = svgCanvas.getSize();
				start = true;
			}
		}
		return start;
	}

	public boolean endInteraction() {
		return finished;
	}

	public void mousePressed(final MouseEvent e) {
		if (startInteraction(e)) {
			finished = false;
			task = new TimerTask() {
				public void run() {
					double x = scrollX;
					double y = scrollY;
					if (x != 0 || y != 0) {
						JGVTComponent c = (JGVTComponent) e.getSource();
						AffineTransform rt = (AffineTransform) c
								.getRenderingTransform().clone();
						double currentTranslateX = rt.getTranslateX();
						double currentTranslateY = rt.getTranslateY();
						// the tranlation that will show the east edge
						double maxTranslateX = -((canvasSize.width * rt
								.getScaleX()) - canvasSize.width);
						// the translation that will show the south
						double maxTranslateY = -((canvasSize.height * rt
								.getScaleY()) - canvasSize.height);

						if ((x > 0 && currentTranslateX + x > 0)) {
							// scroll left && not at west edge
							x = -currentTranslateX;
						} else if (x < 0
								&& currentTranslateX + x < maxTranslateX) {
							// scroll right && not at east edge
							x = maxTranslateX - currentTranslateX;
						}
						if ((y > 0 && currentTranslateY + y > 0)) {
							// scroll up && not at north edge
							y = -currentTranslateY;
						} else if (y < 0
								&& currentTranslateY + y < maxTranslateY) {
							// scroll down && not at south edge
							y = maxTranslateY - currentTranslateY;
						}

						if (x != 0d || y != 0d) {
							AffineTransform at = AffineTransform
									.getTranslateInstance(x, y);
							rt.preConcatenate(at);
							c.setRenderingTransform(rt);
							dispatchDragEvent(x, y);
						}
					}
				}
			};
			timer.schedule(task, 0, SCROLL_INTERVAL);
		}
	}

	/**
	 * Dispatches a mouse drag event that updates the mouse location by the
	 * amount that the canvas has been scrolled.
	 * 
	 * @param dragX
	 * @param dragY
	 */
	private void dispatchDragEvent(double dragX, double dragY) {
		int x = (int) (mouseX + dragX);
		int y = (int) (mouseY + dragY);
		MouseEvent mouseDragEvent = new MouseEvent(eventIdentifier,
				MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(),
				MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false,
				MouseEvent.BUTTON1);
		svgCanvas.dispatchEvent(mouseDragEvent);
	}

	public void mouseReleased(MouseEvent e) {
		if (!finished) {
			finished = true;
			scrollX = 0;
			scrollY = 0;
			if (task != null) {
				task.cancel();
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (!finished) {
			// ignore events generated by this class
			if (e.getSource() != eventIdentifier) {
				mouseX = e.getX();
				mouseY = e.getY();
				int minX = BORDER;
				int maxX = canvasSize.width - BORDER;
				int minY = BORDER;
				int maxY = canvasSize.height - BORDER;

				if (mouseX < minX) {
					scrollX = minX - mouseX;
				} else if (mouseX > maxX) {
					scrollX = maxX - mouseX;
				} else {
					scrollX = 0;
				}
				if (mouseY < minY) {
					scrollY = minY - mouseY;
				} else if (mouseY > maxY) {
					scrollY = maxY - mouseY;
				} else {
					scrollY = 0;
				}
			}
		}
	}

}
