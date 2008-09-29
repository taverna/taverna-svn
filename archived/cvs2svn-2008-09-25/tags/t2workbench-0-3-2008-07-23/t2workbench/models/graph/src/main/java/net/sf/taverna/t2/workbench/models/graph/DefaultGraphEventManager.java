package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.menu.impl.ContextMenuFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Manager for handling UI events on GraphElements.
 * 
 * @author David Withers
 */
public class DefaultGraphEventManager implements GraphEventManager {

	private GraphController graphController;

	private Component component;

	private JPopupMenu menu;

	/**
	 * Constructs a new instance of GraphEventManager.
	 *
	 * @param graphController
	 * @param component component to use when displaying popup menus
	 */
	public DefaultGraphEventManager(GraphController graphController, Component component) {
		this.graphController = graphController;
		this.component = component;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseClicked(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x, final int y, int screenX, int screenY) {
		if (button == 0) {
			if (graphController.getDataflowSelectionModel() != null) {
				graphController.getDataflowSelectionModel().addSelection(graphElement.getDataflowObject());
			}
		} else if (button == 2) {
			if (graphElement instanceof GraphNode && graphElement.getDataflowObject() instanceof Processor) {
				menu = ContextMenuFactory.getContextMenu(graphController.getDataflow(), graphElement.getDataflowObject(), component);
				if (menu == null) {
					menu = new JPopupMenu();
				}
//				menu.add(new JLabel(graphElement.getLabel()));
				menu.addSeparator();
				GraphNode graphNode = (GraphNode) graphElement;
				List<GraphNode> sourceNodes = graphNode.getSourceNodes();
				if (sourceNodes.size() > 0) {
					if (sourceNodes.size() == 1) {
						final GraphNode sourceNode = sourceNodes.get(0);
						menu.add(new JMenuItem(new AbstractAction("Link from output '" + sourceNode.getLabel() + "'") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.startEdgeCreation(sourceNode, new Point(x, y));
							}

						}));	
					} else {
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
				}
				List<GraphNode> sinkNodes = graphNode.getSinkNodes();
				if (sinkNodes.size() > 0) {
					if (sinkNodes.size() == 1) {
						final GraphNode sinkNode = sinkNodes.get(0);
						menu.add(new JMenuItem(new AbstractAction("Link to input '" + sinkNode.getLabel() + "'") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.startEdgeCreation(sinkNode, new Point(x, y));
							}

						}));	
					} else {
						JMenu linkMenu = new JMenu("Link to input...");
						menu.add(linkMenu);
						for (final GraphNode sinkNode : sinkNodes) {
							linkMenu.add(new JMenuItem(new AbstractAction(sinkNode.getLabel()) {

								public void actionPerformed(ActionEvent arg0) {
									graphController.startEdgeCreation(sinkNode, new Point(x, y));
								}

							}));
						}
					}
				}
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				menu.show(component, p.x, p.y);
			} else if (graphElement.getDataflowObject() instanceof DataflowInputPort) {
				Dataflow dataflow = graphController.getDataflow();
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				ContextMenuFactory.getContextMenu(dataflow, graphElement.getDataflowObject(), component).show(component, p.x, p.y);
			} else if (graphElement.getDataflowObject() instanceof DataflowOutputPort) {
				Dataflow dataflow = graphController.getDataflow();
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				ContextMenuFactory.getContextMenu(dataflow, graphElement.getDataflowObject(), component).show(component, p.x, p.y);
			} else if (graphElement.getDataflowObject() instanceof Datalink) {
				Dataflow dataflow = graphController.getDataflow();
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				ContextMenuFactory.getContextMenu(dataflow, graphElement.getDataflowObject(), component).show(component, p.x, p.y);
			} else if (graphElement.getDataflowObject() == null) {
				Dataflow dataflow = graphController.getDataflow();
				Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				ContextMenuFactory.getContextMenu(dataflow, dataflow, component).show(component, p.x, p.y);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseDown(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y, int screenX, int screenY) {
		if (button == 0) {
			graphController.startEdgeCreation(graphElement, new Point(x, y));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseUp(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x,
			final int y, int screenX, int screenY) {
		if (button == 0) {
			graphController.stopEdgeCreation(graphElement, new Point(screenX, screenY));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseMoved(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y, int screenX, int screenY) {
		graphController.moveEdgeCreationTarget(graphElement, new Point(x, y));
	}

}
