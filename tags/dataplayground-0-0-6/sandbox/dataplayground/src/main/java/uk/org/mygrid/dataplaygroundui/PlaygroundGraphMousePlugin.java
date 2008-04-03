package uk.org.mygrid.dataplaygroundui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;
import uk.org.mygrid.dataplayground.PlaygroundProcessorObject;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.visualization.ArrowFactory;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.Paintable;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;

public class PlaygroundGraphMousePlugin extends AbstractGraphMousePlugin
		implements MouseListener, MouseMotionListener, KeyListener {

	private PlaygroundInputPanel playgroundInputPanel;
	private PlaygroundRendererPanel playgroundRendererPanel;
	private PlaygroundMobyPanel playgroundMobyPanel;
	
	SettableVertexLocationFunction vertexLocations;
	Vertex startVertex;
	Vertex highlightedVertex;
	Point2D down;
	CubicCurve2D rawEdge = new CubicCurve2D.Float();
	Shape edgeShape;
	Shape rawArrowShape;
	Shape arrowShape;
	Paintable edgePaintable;
	Paintable arrowPaintable;
	boolean dragging = false;

	public PlaygroundGraphMousePlugin() {
		this(MouseEvent.BUTTON1_MASK);
	}

	/**
	 * Code based on EditingGraphMousePlugin from Jung graph library version
	 * 1.7.6 original author: Tom Nelson - RABA Technologies
	 * 
	 */

	public PlaygroundGraphMousePlugin(int modifiers) {
		super(modifiers);
		rawEdge.setCurve(0.0f, 0.0f, 0.33f, 100, .66f, -50, 1.0f, 0.0f);
		rawArrowShape = ArrowFactory.getNotchedArrow(20, 16, 8);
		edgePaintable = new EdgePaintable();
		arrowPaintable = new ArrowPaintable();
		playgroundInputPanel = (PlaygroundInputPanel) PlaygroundInputPanel
				.getInstance();
		playgroundRendererPanel = (PlaygroundRendererPanel) PlaygroundRendererPanel
				.getInstance();
		playgroundMobyPanel = (PlaygroundMobyPanel) PlaygroundMobyPanel
				.getInstance();
	}

	public boolean checkModifiers(MouseEvent e) {
		return (e.getModifiers() & modifiers) != 0;
	}

	public void keyPressed(KeyEvent e) {

		System.out.println("KeyCode = " + e.getKeyCode());

		if (e.getKeyCode() == KeyEvent.VK_DELETE) {

			VisualizationViewer vv = (VisualizationViewer) e.getSource();
			Layout layout = vv.getGraphLayout();
			Graph graph = layout.getGraph();
			PickedState pS = vv.getPickedState();

			Set pickedVerticies = pS.getPickedVertices();

			for (Iterator i = pickedVerticies.iterator(); i.hasNext();) {

				Vertex v = (Vertex) i.next();
				pS.pick(v, false);
				graph.removeVertex(v);

			}
		}

	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_DELETE) {

			VisualizationViewer vv = (VisualizationViewer) e.getSource();
			Layout layout = vv.getGraphLayout();
			Graph graph = layout.getGraph();
			PickedState pS = vv.getPickedState();

			Set pickedVerticies = pS.getPickedVertices();

			for (Iterator i = pickedVerticies.iterator(); i.hasNext();) {

				Vertex v = (Vertex) i.next();
				pS.pick(v, false);
				graph.removeVertex(v);

			}
		}

	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent e) {

		if (startVertex != null) {
			if (dragging == true) {
				if (startVertex instanceof PlaygroundDataThing) {
					transformEdgeShape(down, e.getPoint());
					transformArrowShape(down, e.getPoint());
				}
				if (startVertex instanceof PlaygroundDataObject) {
					transformEdgeShape(down, e.getPoint());
					transformArrowShape(down, e.getPoint());
				}

			}
			if (startVertex instanceof PlaygroundDataObject) {

				VisualizationViewer vv = (VisualizationViewer) e.getSource();
				PickSupport pickSupport = vv.getPickSupport();
				Point2D p = vv.inverseViewTransform(e.getPoint());
				if (pickSupport != null) {

					// method to get the closest vertex to the mouse point
					// excluding the given vertex
					Vertex vertex = ((PlaygroundPickSupport) pickSupport)
							.getVertex(p.getX(), p.getY(), startVertex);

					if (vertex != null) {
						if (vertex instanceof PlaygroundProcessorObject) {

							highlightedVertex = vertex;
							((PlaygroundVertexPaintFunction) ((PluggableRenderer) vv
									.getRenderer()).getVertexPaintFunction())
									.setHighlighted(vertex);
						} else if (vertex instanceof PlaygroundPortObject) {
							highlightedVertex = vertex;
							((PlaygroundVertexPaintFunction) ((PluggableRenderer) vv
									.getRenderer()).getVertexPaintFunction())
									.setHighlighted(vertex);
						} else {
							((PlaygroundVertexPaintFunction) ((PluggableRenderer) vv
									.getRenderer()).getVertexPaintFunction())
									.setHighlighted(null);
						}

					}
				}

			}

		}
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {

		final VisualizationViewer vv = (VisualizationViewer) e.getSource();
		final Point2D p = vv.inverseViewTransform(e.getPoint());
		PickSupport pickSupport = vv.getPickSupport();
		if (pickSupport != null) {

			final Vertex vertex = pickSupport.getVertex(p.getX(), p.getY());

			if (vertex != null) {

				if (vertex instanceof PlaygroundDataThing) {

					playgroundInputPanel.setData((PlaygroundDataThing) vertex);
					playgroundRendererPanel
							.select((PlaygroundDataThing) vertex);

					if ((e.getModifiers() & MouseEvent.ALT_MASK) != 0) {
						dragging = true;
						startVertex = vertex;
						down = e.getPoint();
						transformEdgeShape(down, down);
						vv.addPostRenderPaintable(edgePaintable);
						transformArrowShape(down, e.getPoint());
						vv.addPostRenderPaintable(arrowPaintable);
					}
				}

				if (vertex instanceof PlaygroundDataObject) {

					// System.out.println("hello 2");
					startVertex = vertex;
					down = e.getPoint();
					if ((e.getModifiers() & MouseEvent.ALT_MASK) != 0) {
						dragging = true;
						startVertex = vertex;
						down = e.getPoint();
						transformEdgeShape(down, down);
						vv.addPostRenderPaintable(edgePaintable);
						transformArrowShape(down, e.getPoint());
						vv.addPostRenderPaintable(arrowPaintable);
					}

				}

			}
		}
	}

	public void mouseReleased(MouseEvent e) {

		final VisualizationViewer vv = (VisualizationViewer) e.getSource();
		final Point2D p = vv.inverseViewTransform(e.getPoint());
		PickSupport pickSupport = vv.getPickSupport();

		if (pickSupport != null) {
			final Vertex vertex = pickSupport.getVertex(p.getX(), p.getY());

			if (vertex != null && startVertex != null) {

				if (dragging == true) {
					if (startVertex instanceof PlaygroundDataThing
							&& vertex instanceof PlaygroundDataObject) {
						Graph graph = vv.getGraphLayout().getGraph();
						graph.addEdge(new DirectedSparseEdge(startVertex,
								vertex));

						vv.repaint();
					}
					if (startVertex instanceof PlaygroundDataThing
							&& vertex instanceof PlaygroundPortObject) {
						Graph graph = vv.getGraphLayout().getGraph();
						graph.addEdge(new DirectedSparseEdge(startVertex,
								vertex));
						((PlaygroundPortObject) vertex)
								.setMappedObject((PlaygroundDataThing) startVertex);
						System.out.println("port object mapped");
						vv.repaint();
					}
					if (startVertex instanceof PlaygroundDataObject
							&& vertex instanceof PlaygroundPortObject) {

						Graph graph = vv.getGraphLayout().getGraph();
						graph.addEdge(new DirectedSparseEdge(startVertex,
								vertex));
						((PlaygroundPortObject) vertex)
								.setMappedObject((PlaygroundDataObject) startVertex);
						vv.repaint();

					}
				} else {

					if (startVertex instanceof PlaygroundDataObject
							&& vertex instanceof PlaygroundProcessorObject) {

						Object[] options = { "Continue", "Cancel" };
						int n = JOptionPane.showOptionDialog(null,
								"Do you wish to annotate the service with this \n"
										+ "moby data type?",
								"Annotate with data type?",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[1]);
						if (n == 0) {
							((PlaygroundProcessorObject) vertex)
									.addInputType(((PlaygroundDataObject) startVertex)
											.getProcessor());
							// vertexLocations.setLocation(vertex,down);
						}
					}
				}

			}

		}
		startVertex = null;
		down = null;
		dragging = false;
		vv.removePostRenderPaintable(edgePaintable);
		vv.removePostRenderPaintable(arrowPaintable);
		((PlaygroundVertexPaintFunction) ((PluggableRenderer) vv.getRenderer())
				.getVertexPaintFunction()).setHighlighted(null);

	}

	public void setVertexLocations(
			SettableVertexLocationFunction vertexLocations) {
		this.vertexLocations = vertexLocations;
	}

	private void transformArrowShape(Point2D down, Point2D out) {
		float x1 = (float) down.getX();
		float y1 = (float) down.getY();
		float x2 = (float) out.getX();
		float y2 = (float) out.getY();

		AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);

		float dx = x2 - x1;
		float dy = y2 - y1;
		float thetaRadians = (float) Math.atan2(dy, dx);
		xform.rotate(thetaRadians);
		arrowShape = xform.createTransformedShape(rawArrowShape);
	}

	/**
	 * code extracted from PluggableRenderer to move an edge shape into an
	 * arbitrary position
	 */
	private void transformEdgeShape(Point2D down, Point2D out) {
		float x1 = (float) down.getX();
		float y1 = (float) down.getY();
		float x2 = (float) out.getX();
		float y2 = (float) out.getY();

		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		float dx = x2 - x1;
		float dy = y2 - y1;
		float thetaRadians = (float) Math.atan2(dy, dx);
		xform.rotate(thetaRadians);
		float dist = (float) Math.sqrt(dx * dx + dy * dy);
		xform.scale(dist / rawEdge.getBounds().getWidth(), 1.0);
		edgeShape = xform.createTransformedShape(rawEdge);
	}

	/**
	 * code lifted from EditingGraphMouse Plugin Used for the directed edge
	 * creation visual effect during mouse drag
	 */
	class ArrowPaintable implements Paintable {

		public void paint(Graphics g) {
			if (arrowShape != null) {
				Color oldColor = g.getColor();
				g.setColor(Color.black);
				((Graphics2D) g).fill(arrowShape);
				g.setColor(oldColor);
			}
		}

		public boolean useTransform() {
			return false;
		}
	}

	/**
	 * code lifted from EditingGraphMouse Plugin Used for the edge creation
	 * visual effect during mouse drag
	 */
	class EdgePaintable implements Paintable {

		public void paint(Graphics g) {
			if (edgeShape != null) {
				Color oldColor = g.getColor();
				g.setColor(Color.black);
				((Graphics2D) g).draw(edgeShape);
				g.setColor(oldColor);
			}
		}

		public boolean useTransform() {
			return false;
		}
	}

}
