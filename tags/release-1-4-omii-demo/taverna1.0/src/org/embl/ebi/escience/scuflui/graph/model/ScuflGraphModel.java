/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.graph.GraphColours;
import org.embl.ebi.escience.scuflui.graph.GraphUtilities;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;

/**
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.26 $
 */
public class ScuflGraphModel implements GraphModel, GraphModelListener, ScuflUIComponent
{
	private class RaisedBorder extends LineBorder
	{
		/**
		 */
		public RaisedBorder()
		{
			super(null);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			Color oldColor = g.getColor();
			// g.setColor(c.getBackground());
			// g.draw3DRect(x, y, width, height, true);
			g.setColor(c.getBackground().brighter());
			g.drawLine(x, y, x, y + height);
			g.drawLine(x + 1, y, x + width - 1, y);
			g.setColor(c.getBackground().darker());
			g.drawLine(x + 1, y + height - 1, x + width, y + height - 1);
			g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
			g.setColor(oldColor);
		}
	}

	private class DummyPort
	{
		private Object parent;

		/**
		 * @param parent
		 */
		public DummyPort(Object parent)
		{
			this.parent = parent;
		}

		public String toString()
		{
			return "DummyPort[" + parent + "]";
		}

		/**
		 * @return processor
		 */
		public Object getParent()
		{
			return parent;
		}
	}

	private static final String PORT_EDGES = "port edges";
	private static final String DUMMY_PORT = "dummy port";

	private boolean showBoring = true;
	private ScuflModel model;
	private ScuflModelReconciler reconciler;
	List roots = new ArrayList();
	private Map attributes = new HashMap();

	private Collection listeners = new HashSet();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getRootCount()
	 */
	public int getRootCount()
	{
		return roots.size();
	}

	/**
	 * @return the ScuflModel
	 */
	public ScuflModel getModel()
	{
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getRootAt(int)
	 */
	public Object getRootAt(int index)
	{
		return roots.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getIndexOfRoot(java.lang.Object)
	 */
	public int getIndexOfRoot(Object root)
	{
		return roots.indexOf(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#contains(java.lang.Object)
	 */
	public boolean contains(Object node)
	{
		return roots.contains(GraphUtilities.getRoot(this, node));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getAttributes(java.lang.Object)
	 */
	public AttributeMap getAttributes(Object node)
	{
		if (node != null)
		{
			return (AttributeMap) attributes.get(node);
		}
		return (AttributeMap) attributes.get(this);
	}

	private DummyPort getDummyPort(Object node)
	{
		Map attrs = getAttributes(node);
		DummyPort port = (DummyPort) attrs.get(DUMMY_PORT);
		if (port == null)
		{
			port = new DummyPort(node);
			attrs.put(DUMMY_PORT, port);
			addAttributes(port);
		}
		return port;
	}

	/**
	 * @param node
	 * @return created AttributeMap
	 */
	private AttributeMap addAttributes(Object node)
	{
		AttributeMap map = getAttributes(node);
		if(map == null)
		{
			map = new AttributeMap();
			attributes.put(node, map);
		}

		GraphConstants.setValue(map, getValue(node));
		if (node instanceof Processor)
		{
			Processor processor = (Processor) node;
			if (processor == model.getWorkflowSourceProcessor())
			{
				GraphConstants.setOpaque(map, true);
				GraphConstants.setBorder(map, new TitledBorder(new LineBorder(Color.GRAY),
						"Inputs"));
				GraphConstants.setEditable(map, false);
			}
			else if (processor == model.getWorkflowSinkProcessor())
			{
				GraphConstants.setOpaque(map, true);
				GraphConstants.setBorder(map, new TitledBorder(new LineBorder(Color.GRAY),
						"Outputs"));
				GraphConstants.setEditable(map, false);
			}
			else
			{
				GraphConstants.setBounds(map, new Rectangle(100, 20));
				GraphConstants.setBackground(map, GraphColours.getColour(ProcessorHelper
						.getPreferredColour(processor), Color.WHITE));
				GraphConstants.setOpaque(map, true);
				GraphConstants.setResize(map, true);
				GraphConstants.setBorder(map, new CompoundBorder(new RaisedBorder(),
						new EmptyBorder(2, 7, 2, 7)));
			}
		}
		else if (node instanceof Port)
		{
			if (isPortOnWorkflowEdge(node))
			{
				GraphConstants.setBounds(map, new Rectangle(100, 20));
				if (node instanceof InputPort)
				{
					GraphConstants.setBackground(map, GraphColours.getColour("lightsteelblue2",
							Color.WHITE));
				}
				else
				{
					GraphConstants.setBackground(map, GraphColours
							.getColour("skyblue", Color.WHITE));
				}
				GraphConstants.setOpaque(map, true);
				GraphConstants.setResize(map, true);
				GraphConstants.setBorder(map, new CompoundBorder(new RaisedBorder(),
						new EmptyBorder(2, 7, 2, 7)));
			}
			GraphConstants.setDisconnectable(map, false);
		}
		else if (node instanceof DataConstraint)
		{
			GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC);
			GraphConstants.setEndFill(map, true);
			GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
			GraphConstants.setDisconnectable(map, false);
			GraphConstants.setEditable(map, false);

			List defaultPoints = new ArrayList();
			defaultPoints.add(getSource(node));
			defaultPoints.add(getTarget(node));
			GraphConstants.setPoints(map, defaultPoints);

			GraphConstants.setLabelAlongEdge(map, false);
		}
		else if (node instanceof ConcurrencyConstraint)
		{
			GraphConstants.setLineEnd(map, GraphConstants.ARROW_CIRCLE);
			GraphConstants.setEndFill(map, false);
			GraphConstants.setEndSize(map, 6);
			GraphConstants.setLineColor(map, Color.LIGHT_GRAY);
			GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
			GraphConstants.setDisconnectable(map, false);
			GraphConstants.setEditable(map, false);

			List defaultPoints = new ArrayList();
			defaultPoints.add(getSource(node));
			defaultPoints.add(getTarget(node));
			GraphConstants.setPoints(map, defaultPoints);

			GraphConstants.setLabelAlongEdge(map, false);
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getSource(java.lang.Object)
	 */
	public Object getSource(Object edge)
	{
		if (edge instanceof DataConstraint)
		{
			Object source = ((DataConstraint) edge).getSource();
			if (isPortOnWorkflowEdge(source))
			{
				return getDummyPort(source);
			}
			return source;
		}
		else if (edge instanceof ConcurrencyConstraint)
		{
			return getDummyPort(((ConcurrencyConstraint) edge).getControllingProcessor());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getTarget(java.lang.Object)
	 */
	public Object getTarget(Object edge)
	{
		if (edge instanceof DataConstraint)
		{
			Object target = ((DataConstraint) edge).getSink();
			if (isPortOnWorkflowEdge(target))
			{
				return getDummyPort(target);
			}
			return target;
		}
		else if (edge instanceof ConcurrencyConstraint)
		{
			return getDummyPort(((ConcurrencyConstraint) edge).getTargetProcessor());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#acceptsSource(java.lang.Object, java.lang.Object)
	 */
	public boolean acceptsSource(Object edge, Object port)
	{
		if (edge instanceof DataConstraint)
		{
			return port instanceof OutputPort
					|| (port instanceof DummyPort && ((DummyPort) port).getParent() instanceof OutputPort);
		}
		return edge instanceof ConcurrencyConstraint && port instanceof DummyPort
				&& ((DummyPort) port).getParent() instanceof Processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#acceptsTarget(java.lang.Object, java.lang.Object)
	 */
	public boolean acceptsTarget(Object edge, Object port)
	{
		if (edge instanceof DataConstraint)
		{
			return port instanceof InputPort
					|| (port instanceof DummyPort && ((DummyPort) port).getParent() instanceof InputPort);
		}
		return edge instanceof ConcurrencyConstraint && port instanceof DummyPort
				&& ((DummyPort) port).getParent() instanceof Processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#edges(java.lang.Object)
	 */
	public Iterator edges(Object port)
	{
		if (port instanceof Port || port instanceof DummyPort)
		{
			Map attributes = getAttributes(port);
			if (attributes != null)
			{
				Collection edges = (Collection) attributes.get(PORT_EDGES);
				if (edges != null)
				{
					return edges.iterator();
				}
			}
		}
		return Collections.EMPTY_LIST.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#isEdge(java.lang.Object)
	 */
	public boolean isEdge(Object edge)
	{
		return edge instanceof DataConstraint || edge instanceof ConcurrencyConstraint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#isPort(java.lang.Object)
	 */
	public boolean isPort(Object port)
	{
		return (port instanceof Port && !isPortOnWorkflowEdge(port)) || port instanceof DummyPort;
	}

	private boolean isPortOnWorkflowEdge(Object port)
	{
		if (port instanceof InputPort)
		{
			return ((Port) port).getProcessor() == model.getWorkflowSinkProcessor();
		}
		else if (port instanceof OutputPort)
		{
			return ((Port) port).getProcessor() == model.getWorkflowSourceProcessor();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getParent(java.lang.Object)
	 */
	public Object getParent(Object child)
	{
		if (child instanceof Port)
		{
			return ((Port) child).getProcessor();
		}
		else if (child instanceof DummyPort)
		{
			return ((DummyPort) child).getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent instanceof Processor)
		{
			Port[] ports = ((Processor) parent).getPorts();
			for (int index = 0; index < ports.length; index++)
			{
				if (ports[index].equals(child))
				{
					return index;
				}
			}
		}
		else if (parent instanceof Port)
		{
			// Check port is actually on the workflow edge?
			return 0;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index)
	{
		if (parent instanceof Processor)
		{
			Processor processor = (Processor) parent;
			Port[] ports = processor.getPorts();
			if (index < ports.length)
			{
				return ports[index];
			}
			return getDummyPort(processor);
		}
		else if (parent instanceof Port)
		{
			// TODO Check port is actually on the workflow edge?
			return getDummyPort(parent);
		}
		else if (parent instanceof List)
		{
			return ((List) parent).get(index);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent)
	{
		if (parent instanceof Processor)
		{
			int ports = ((Processor) parent).getPorts().length;
			if (getAttributes(parent) != null && getAttributes(parent).get(DUMMY_PORT) != null)
			{
				ports += 1;
			}
			return ports;
		}
		else if (parent instanceof Port && isPortOnWorkflowEdge(parent))
		{
			return 1;
		}
		else if (parent instanceof List)
		{
			return ((List) parent).size();
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node)
	{
		if (node instanceof Processor)
		{
			return ((Processor) node).getPorts().length != 0;
		}
		else if (node instanceof Port && isPortOnWorkflowEdge(node))
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#insert(java.lang.Object[], java.util.Map,
	 *      org.jgraph.graph.ConnectionSet, org.jgraph.graph.ParentMap,
	 *      javax.swing.undo.UndoableEdit[])
	 */
	public void insert(Object[] cells, Map attributes, ConnectionSet cs, ParentMap pm,
			UndoableEdit[] e)
	{
		updateAttributes(attributes);
		// fireGraphChangedEvent(new ScuflGraphModelChange(this, null, null,
		// null, attributes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#remove(java.lang.Object[])
	 */
	public void remove(Object[] cells)
	{
		for (int index = 0; index < cells.length; index++)
		{
			if (cells[index] instanceof Processor)
			{
				if (cells[index] != model.getWorkflowSinkProcessor()
						&& cells[index] != model.getWorkflowSourceProcessor())
				{
					model.destroyProcessor((Processor) cells[index]);
				}
			}
			else if (cells[index] instanceof DataConstraint)
			{
				model.destroyDataConstraint((DataConstraint) cells[index]);
			}
			else if (cells[index] instanceof ConcurrencyConstraint)
			{
				model.destroyConcurrencyConstraint((ConcurrencyConstraint) cells[index]);
			}
			else if (cells[index] instanceof Port)
			{
				// TODO Check port is actually on the workflow edge?
				Port port = (Port) cells[index];
				port.getProcessor().removePort(port);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#edit(java.util.Map, org.jgraph.graph.ConnectionSet,
	 *      org.jgraph.graph.ParentMap, javax.swing.undo.UndoableEdit[])
	 */
	public void edit(Map attributes, ConnectionSet cs, ParentMap pm, UndoableEdit[] e)
	{
		if (attributes != null && !attributes.isEmpty())
		{
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				Map map = (Map) entry.getValue();
				Object value = GraphConstants.getValue(map);
				if (value != null)
				{
					Object result = valueForCellChanged(cell, value);
					if(value.equals(result))
					{
						map.remove(GraphConstants.VALUE);
					}
					else
					{
						GraphConstants.setValue(map, result);
						GraphConstants.setResize(map, true);
					}
				}
			}
			if (!attributes.isEmpty())
			{
				updateAttributes(attributes);
				fireGraphChangedEvent(new GraphModelEvent(this, new ScuflGraphAttributeChange(
						attributes)));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#cloneCells(java.lang.Object[])
	 */
	public Map cloneCells(Object[] cells)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#toBack(java.lang.Object[])
	 */
	public void toBack(Object[] cells)
	{
		// Not used?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#toFront(java.lang.Object[])
	 */
	public void toFront(Object[] cells)
	{
		// Not used?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#addGraphModelListener(org.jgraph.event.GraphModelListener)
	 */
	public void addGraphModelListener(GraphModelListener l)
	{
		listeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#removeGraphModelListener(org.jgraph.event.GraphModelListener)
	 */
	public void removeGraphModelListener(GraphModelListener l)
	{
		listeners.remove(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#addUndoableEditListener(javax.swing.event.UndoableEditListener)
	 */
	public void addUndoableEditListener(UndoableEditListener listener)
	{
		// Not used?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#removeUndoableEditListener(javax.swing.event.UndoableEditListener)
	 */
	public void removeUndoableEditListener(UndoableEditListener listener)
	{
		// Not used?
	}

	private void fireGraphChangedEvent(GraphModelEvent event)
	{
		Iterator listenerIterator = listeners.iterator();
		while (listenerIterator.hasNext())
		{
			GraphModelListener listener = (GraphModelListener) listenerIterator.next();
			listener.graphChanged(event);
		}
	}

	void updateAttributes(Map attributes)
	{
		if (attributes != null)
		{
			Iterator iterator = attributes.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry entry = (Map.Entry) iterator.next();
				Object node = entry.getKey();
				Map newAttributes = (Map) entry.getValue();
				AttributeMap attr = getAttributes(node);
				if (attr != null)
				{
					attr.putAll(newAttributes);
				}
			}
		}
	}

	private void addEdge(Object port, Object edge)
	{
		Map attrs = getAttributes(port);
		Collection edges = (Collection) attrs.get(PORT_EDGES);
		if (edges == null)
		{
			edges = new HashSet();
			attrs.put(PORT_EDGES, edges);
		}
		edges.add(edge);
	}

	/**
	 * @param port
	 * @param constraint
	 */
	private void removeEdge(Object port, Object constraint)
	{
		Map attrs = getAttributes(port);
		if (attrs != null)
		{
			Collection edges = (Collection) attrs.get(PORT_EDGES);
			if (edges != null)
			{
				edges.remove(constraint);
			}
		}
	}

	Map addNode(Object newNode, ConnectionSet cs)
	{
		if(getParent(newNode) == null && !roots.contains(newNode))
		{
			roots.add(newNode);
		}
		Map attributes = addAttributes(newNode);
		if (isEdge(newNode))
		{
			Object source = getSource(newNode);
			Object target = getTarget(newNode);
			cs.connect(newNode, source, target);
			addEdge(source, newNode);
			addEdge(target, newNode);
		}
		return attributes;
	}

	void removeNode(Object node)
	{
		if(getParent(node) == null)
		{
			roots.remove(node);
		}		
		if (isEdge(node))
		{
			removeEdge(getSource(node), node);
			removeEdge(getTarget(node), node);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public void graphChanged(GraphModelEvent event)
	{
		GraphModelEvent.GraphModelChange change = event.getChange();
		if (change instanceof ScuflGraphModelChange)
		{
			((ScuflGraphModelChange) change).execute();
		}
		fireGraphChangedEvent(event);
		Object[] removed = change.getRemoved();
		for (int index = 0; index < removed.length; index++)
		{
			attributes.remove(removed[index]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(ScuflModel model)
	{
		this.model = model;
		addAttributes(this);
		reconciler = new ScuflModelReconciler(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel()
	{
		reconciler.detachFromModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getName()
	 */
	public String getName()
	{
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon()
	{
		return null;
	}

	public boolean isShowingBoring()
	{
		return showBoring;
	}
	
	/**
	 * @param showBoring
	 */
	public void setShowBoring(boolean showBoring)
	{
		if(this.showBoring != showBoring)
		{
			this.showBoring = showBoring;

			if(model != null)
			{
				ScuflGraphModelChange change = new ScuflGraphModelChange(this);
				change.addChanges(new ScuflModelEvent(model, "Changed show boring"));
				change.execute();			
				graphChanged(new GraphModelEvent(model, change));
			}
		}
	}

	public Object getValue(Object node)
	{
		if(node instanceof Processor)
		{
			return ((Processor)node).getName();
		}
		else if(node instanceof Port)
		{
			return ((Port)node).getName();
		}
		return "";
	}

	public Object valueForCellChanged(Object cell, Object newValue)
	{
		if (cell instanceof Processor)
		{
			Processor processor = (Processor) cell;
			processor.setName(newValue.toString());
			return processor.getName();
		}
		return null;
	}
}