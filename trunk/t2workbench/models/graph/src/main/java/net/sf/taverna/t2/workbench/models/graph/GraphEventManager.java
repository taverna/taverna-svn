package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Manager for handling UI events on GraphElements.
 * 
 * @author David Withers
 */
public class GraphEventManager {

	private GraphController graphController;

	private JComponent component;

	private JPopupMenu menu = new JPopupMenu();

	/**
	 * Constructs a new instance of GraphEventManager.
	 *
	 * @param graphController
	 * @param component component to use when displaying popup menus
	 */
	public GraphEventManager(GraphController graphController, JComponent component) {
		this.graphController = graphController;
		this.component = component;
		menu.setLightWeightPopupEnabled(false);
	}

	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x, final int y, int screenX, int screenY) {
		if (button == 0) {
			graphController.getDataflowSelectionModel().addSelection(graphElement.getDataflowObject());
		} else if (button == 2) {
			if (graphElement instanceof GraphNode) {
				menu.removeAll();
				menu.setLabel(graphElement.getLabel());
				menu.addSeparator();
				GraphNode graphNode = (GraphNode) graphElement;
				List<GraphNode> sourceNodes = graphNode.getSourceNodes();
				if (sourceNodes.size() > 0) {
					JMenu linkMenu = new JMenu("Link from output...");
					menu.add(linkMenu);
					for (final GraphNode sourceNode : sourceNodes) {
						linkMenu.add(new JMenuItem(new AbstractAction(sourceNode.getLabel()) {
							public void actionPerformed(ActionEvent arg0) {
								graphController.startEdgeCreation(sourceNode, new Point(x, y));
							}

						}));	
					}
				}
				List<GraphNode> sinkNodes = graphNode.getSinkNodes();
				if (sinkNodes.size() > 0) {
					JMenu linkMenu = new JMenu("Link to input...");
					menu.add(linkMenu);
					for (final GraphNode sinkNode : sinkNodes) {
						JMenuItem menuItem = new JMenuItem();
						linkMenu.add(new JMenuItem(new AbstractAction(sinkNode.getLabel()) {

							public void actionPerformed(ActionEvent arg0) {
								graphController.startEdgeCreation(sinkNode, new Point(x, y));
							}

						}));
					}
				}
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				menu.show(component, p.x, p.y);
			}
		}
	}

	public void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y) {
		if (button == 0) {
			graphController.startEdgeCreation(graphElement, new Point(x, y));
		}
	}

	public void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y) {
		if (button == 0) {
			graphController.stopEdgeCreation(graphElement, new Point(x, y));
		}
	}

	public void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y) {
		graphController.moveEdgeCreationTarget(graphElement, new Point(x, y));
	}

}
