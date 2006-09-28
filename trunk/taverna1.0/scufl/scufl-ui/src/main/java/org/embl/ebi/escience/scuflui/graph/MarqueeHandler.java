/*
 * Created on Jan 26, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JPopupMenu;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AddDataConstraintAction;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.VertexView;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 */
public class MarqueeHandler extends BasicMarqueeHandler
{
	private JGraph graph;
	private Port startPort;
	private VertexView startView;
	private Object target = null;

	public void startEdge(JGraph graph, Port startPort)
	{
		this.graph = graph;
		this.startPort = startPort;
		CellView view = graph.getGraphLayoutCache().getMapping(startPort, false);
		startView = getVertexView(view);
	}

	/**
	 * @param view
	 * @return vertex!
	 */
	private VertexView getVertexView(CellView view)
	{
		CellView temp = view;
		while (temp != null)
		{
			if (temp instanceof VertexView)
			{
				return (VertexView) temp;
			}
			temp = temp.getParentView();
		}
		return null;
	}

	public void mouseMoved(MouseEvent e)
	{
		if (startPort != null)
		{
			VertexView cell = getVertexView(graph.getLeafViewAt(e.getX(), e.getY()));
			if (cell != null)
			{
				if (cell.getCell() instanceof Port)
				{
					if (startPort.getClass() != cell.getCell().getClass())
					{
						target = cell;
						graph.setCursor(Cursor.getDefaultCursor());
					}
					else
					{
						target = new Point(e.getX(), e.getY());
						graph.setCursor(DragSource.DefaultLinkNoDrop);
					}
				}
				else
				{
					Processor processor = (Processor) cell.getCell();
					if ((startPort instanceof InputPort && processor.getOutputPorts().length > 0)
							|| processor.getInputPorts().length > 0)
					{
						target = cell;
						graph.setCursor(Cursor.getDefaultCursor());
					}
					else
					{
						target = new Point(e.getX(), e.getY());
						graph.setCursor(DragSource.DefaultLinkNoDrop);
					}
				}
			}
			else
			{
				target = new Point(e.getX(), e.getY());
				graph.setCursor(DragSource.DefaultLinkNoDrop);
			}
			graph.repaint();
		}
		else
		{
			super.mouseMoved(e);
		}
	}

	public void mouseDragged(MouseEvent e)
	{
		if (startPort != null)
		{
			mouseMoved(e);
		}
		else
		{
			super.mouseDragged(e);
		}
	}

	public void mousePressed(MouseEvent e)
	{
		if (startPort == null)
		{
			super.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		if (startPort != null)
		{
			if (target instanceof VertexView)
			{
				if (((VertexView) target).getCell() instanceof Processor)
				{
					Processor processor = (Processor) ((VertexView) target).getCell();
					Port[] ports;
					if (startPort instanceof InputPort)
					{
						ports = processor.getOutputPorts();
					}
					else
					{
						ports = processor.getInputPorts();
					}

					JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new ShadedLabel("Connect to...", ShadedLabel.TAVERNA_GREEN));
					popupMenu.addSeparator();
					for (int index = 0; index < ports.length; index++)
					{
						popupMenu.add(new AddDataConstraintAction(startPort.getProcessor()
								.getModel(), startPort, ports[index]));
					}
					popupMenu.show(graph, e.getX(), e.getY());
				}
				else if (((VertexView) target).getCell() instanceof Port)
				{
					new AddDataConstraintAction(startPort.getProcessor().getModel(), startPort,
							(Port) ((VertexView) target).getCell()).actionPerformed(null);
				}
			}
			startPort = null;
			target = null;
			graph.setCursor(Cursor.getDefaultCursor());
			graph.repaint();
			e.consume();
		}
		else
		{
			super.mouseReleased(e);
		}
	}

	public void overlay(JGraph graph, Graphics g, boolean clear)
	{
		if (startPort != null && target != null)
		{
			Point2D end;
			if (target instanceof VertexView)
			{
				Point2D startCenter = new Point2D.Double(startView.getBounds().getCenterX(),
						startView.getBounds().getCenterY());
				VertexView targetView = (VertexView) target;
				end = targetView.getPerimeterPoint(null, startCenter);
			}
			else
			{
				end = (Point2D) target;
			}
			Point2D start = startView.getPerimeterPoint(null, end);

			Graphics2D graphics = (Graphics2D) g;
			if (graph.isAntiAliased())
			{
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
			}

			int size = 15;
			Point2D dst;
			Point2D src;
			if (startPort instanceof InputPort)
			{
				dst = start;
				src = end;
			}
			else
			{
				dst = end;
				src = start;
			}
			int d = (int) Math.max(1, dst.distance(src));
			int ax = (int) -(size * (dst.getX() - src.getX()) / d);
			int ay = (int) -(size * (dst.getY() - src.getY()) / d);
			Polygon poly = new Polygon();
			poly.addPoint((int) dst.getX(), (int) dst.getY());
			poly.addPoint((int) (dst.getX() + ax + ay / 2), (int) (dst.getY() + ay - ax / 2));
			Point2D last = (Point2D) dst.clone();
			dst.setLocation((int) (dst.getX() + ax * 2 / 3), (int) (dst.getY() + ay * 2 / 3));
			poly.addPoint((int) dst.getX(), (int) dst.getY());
			poly.addPoint((int) (last.getX() + ax - ay / 2), (int) (last.getY() + ay + ax / 2));

			Line2D line = new Line2D.Double(start, end);

			graphics.setColor(Color.WHITE);
			graphics.setStroke(new BasicStroke(2));
			graphics.setPaintMode();
			graphics.draw(line);
			graphics.fill(poly);

			graphics.setColor(Color.BLACK);
			graphics.setStroke(new BasicStroke(1));
			graphics.setPaintMode();
			graphics.draw(line);
			graphics.fill(poly);
		}
		else
		{
			super.overlay(graph, g, clear);
		}
	}

	public boolean isForceMarqueeEvent(MouseEvent event)
	{
		return startPort != null;
	}
}