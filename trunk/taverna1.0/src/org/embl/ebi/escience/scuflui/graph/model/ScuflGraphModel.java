/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.graph.GraphColours;
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
 * @version $Revision: 1.1 $
 */
public class ScuflGraphModel implements GraphModel, GraphModelListener
{
	private class ConcurrencyPort
	{
		private Processor processor;
		private Collection constraints = new HashSet();
		
		/**
		 * @param processor
		 */
		public ConcurrencyPort(Processor processor)
		{
			this.processor = processor;
		}
		
		/**
		 * @return processor
		 */
		public Processor getProcessor()
		{
			return processor;
		}
		
		/**
		 * @param constraint
		 */
		public void addConstraint(ConcurrencyConstraint constraint)
		{
			constraints.add(constraint);
		}
		
		/**
		 * @param constraint
		 */
		public void removeConstraint(ConcurrencyConstraint constraint)
		{
			constraints.remove(constraint);
		}
		
		/**
		 * @return edge iterator
		 */
		public Iterator edges()
		{
			return constraints.iterator();
		}
	}
	
	
	private static final String PORT_EDGES = "port edges";
	private static final String PARENT_PROCESSOR = "parent processor";
	private static final String CONCURRENCY_PORT = "concurrency port";
	
	//private List inputs;
	//private List outputs;
	private Collection listeners = new HashSet();

	private ScuflModel model;
	List roots = new ArrayList();	
	private Map attributes = new HashMap();
	
	/**
	 * @param model
	 *            Scufl model for the graph
	 */
	public ScuflGraphModel(ScuflModel model)
	{
		this.model = model;
		createAttributes(this);		
		new ScuflModelReconciler(this);
	}

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

	private ConcurrencyPort getConcurrencyPort(Processor processor)
	{
		Map attrs = getAttributes(processor);
		ConcurrencyPort port = (ConcurrencyPort)attrs.get(CONCURRENCY_PORT);
		if(port == null)
		{
			port = new ConcurrencyPort(processor);
			attrs.put(CONCURRENCY_PORT, port);
			createAttributes(port);
		}
		return port;
	}
	
	/**
	 * @param node
	 * @return created AttributeMap
	 */
	private AttributeMap createAttributes(Object node)
	{
		AttributeMap map = new AttributeMap();
		if (node instanceof ScuflGraphModel)
		{
			// Bleh
		}
		else if (node instanceof Processor)
		{
			Processor processor = (Processor) node;
			GraphConstants.setValue(map, processor.getName());
			GraphConstants.setBounds(map, new Rectangle(100, 20));
			GraphConstants.setBackground(map, GraphColours.getColour(ProcessorHelper
					.getPreferredColour(processor), Color.WHITE));
			GraphConstants.setOpaque(map, true);
			GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		}
		else if (node instanceof DummyProcessor)
		{
			GraphConstants.setValue(map, node.toString());
			GraphConstants.setBounds(map, new Rectangle(100, 20));
			if (((DummyProcessor) node).getPort() instanceof InputPort)
			{
				GraphConstants.setBackground(map, GraphColours.getColour("lightsteelblue2",
						Color.WHITE));

			}
			else
			{
				GraphConstants.setBackground(map, GraphColours.getColour("skyblue", Color.WHITE));
			}
			GraphConstants.setOpaque(map, true);
			GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		}
		else if (node instanceof Port)
		{
			GraphConstants.setDisconnectable(map, false);
			map.put(PARENT_PROCESSOR, ((Port)node).getProcessor());			
		}
		else if (node instanceof DataConstraint)
		{
			GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC);
			GraphConstants.setEndFill(map, true);
			GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
			GraphConstants.setDisconnectable(map, false);			
			
			List defaultPoints = new ArrayList();
			defaultPoints.add(map.createPoint(10, 10));
			defaultPoints.add(map.createPoint(20, 20));
			GraphConstants.setPoints(map, defaultPoints);

			GraphConstants.setLabelAlongEdge(map, false);
			GraphConstants.setValue(map, "");

			int center = GraphConstants.PERMILLE / 2;
			Point labelPosition = new Point(center, center);
			GraphConstants.setLabelPosition(map, labelPosition);
		}
		else if (node instanceof ConcurrencyConstraint)
		{
			GraphConstants.setLineEnd(map, GraphConstants.ARROW_CIRCLE);
			GraphConstants.setEndFill(map, false);
			GraphConstants.setEndSize(map, 6);
			GraphConstants.setLineColor(map, Color.LIGHT_GRAY);
			GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
			GraphConstants.setDisconnectable(map, false);			

			List defaultPoints = new ArrayList();
			defaultPoints.add(map.createPoint(10, 10));
			defaultPoints.add(map.createPoint(20, 20));
			GraphConstants.setPoints(map, defaultPoints);

			GraphConstants.setLabelAlongEdge(map, false);
			GraphConstants.setValue(map, "");

			int center = GraphConstants.PERMILLE / 2;
			Point labelPosition = new Point(center, center);
			GraphConstants.setLabelPosition(map, labelPosition);
		}
		attributes.put(node, map);
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
			return ((DataConstraint) edge).getSource();
		}
		else if (edge instanceof ConcurrencyConstraint)
		{
			return getConcurrencyPort(((ConcurrencyConstraint) edge).getControllingProcessor());
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
			return ((DataConstraint) edge).getSink();
		}
		else if (edge instanceof ConcurrencyConstraint)
		{
			return getConcurrencyPort(((ConcurrencyConstraint) edge).getTargetProcessor());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#acceptsSource(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean acceptsSource(Object edge, Object port)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#acceptsTarget(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean acceptsTarget(Object edge, Object port)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#edges(java.lang.Object)
	 */
	public Iterator edges(Object port)
	{
		if (port instanceof Port)
		{
			Map attributes = getAttributes(port);
			if(attributes != null)
			{
				Collection edges = (Collection) attributes.get(PORT_EDGES);
				if (edges != null)
				{
					return edges.iterator();
				}
			}
		}
		else if(port instanceof ConcurrencyPort)
		{
			return ((ConcurrencyPort)port).edges();
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
		return port instanceof Port || port instanceof ConcurrencyPort;
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
			Map attrs = (Map)attributes.get(child);
			if(attrs != null)
			{
				return attrs.get(PARENT_PROCESSOR);
			}
		}
		else if(child instanceof ConcurrencyPort)
		{
			return ((ConcurrencyPort)child).getProcessor();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#getIndexOfChild(java.lang.Object,
	 *      java.lang.Object)
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
		else if (parent instanceof DummyProcessor)
		{
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
			return getConcurrencyPort(processor);
		}
		else if (parent instanceof DummyProcessor)
		{
			return ((DummyProcessor) parent).getPort();
		}
		else if(parent instanceof List)
		{
			return ((List)parent).get(index);
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
			return ((Processor) parent).getPorts().length + 1;
		}
		else if (parent instanceof DummyProcessor)
		{
			return 1;
		}
		else if(parent instanceof List)
		{
			return ((List)parent).size();
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
		else if (node instanceof DummyProcessor)
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#insert(java.lang.Object[],
	 *      java.util.Map, org.jgraph.graph.ConnectionSet,
	 *      org.jgraph.graph.ParentMap, javax.swing.undo.UndoableEdit[])
	 */
	public void insert(Object[] cells, Map attributes, ConnectionSet cs, ParentMap pm,
			UndoableEdit[] e)
	{
		updateAttributes(attributes);
		//fireGraphChangedEvent(new ScuflGraphModelChange(this, null, null, null, attributes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#remove(java.lang.Object[])
	 */
	public void remove(Object[] cells)
	{
		for(int index = 0; index < cells.length; index++)
		{
			if(cells[index] instanceof Processor)
			{
				model.destroyProcessor((Processor)cells[index]);
			}
			else if(cells[index] instanceof DataConstraint)
			{
				model.destroyDataConstraint((DataConstraint)cells[index]);
			}
			else if(cells[index] instanceof ConcurrencyConstraint)
			{
				model.destroyConcurrencyConstraint((ConcurrencyConstraint)cells[index]);
			}
			else if(cells[index] instanceof DummyProcessor)
			{
				Port port = ((DummyProcessor)cells[index]).getPort();
				port.getProcessor().removePort(port);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#edit(java.util.Map,
	 *      org.jgraph.graph.ConnectionSet, org.jgraph.graph.ParentMap,
	 *      javax.swing.undo.UndoableEdit[])
	 */
	public void edit(Map attributes, ConnectionSet cs, ParentMap pm, UndoableEdit[] e)
	{
		//TODO Change name of processors on in-place edit
		if(attributes != null && !attributes.isEmpty())
		{
			updateAttributes(attributes);			
			fireGraphChangedEvent(new GraphModelEvent(this, new ScuflGraphAttributeChange(attributes)));
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
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				Map deltaNew = (Map) entry.getValue();
				AttributeMap attr = getAttributes(cell);
				if (attr != null)
				{
					attr.applyMap(deltaNew);
				}
			}
		}
	}

	private void edgeAdded(Object port, Object edge)
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
	private void edgeRemoved(Object port, Object constraint)
	{
		Map attrs = getAttributes(port);
		if(attrs != null)
		{
			Collection edges = (Collection) attrs.get(PORT_EDGES);
			if (edges != null)
			{
				edges.remove(constraint);
			}
		}
	}

	Map nodeAdded(Object newNode, ConnectionSet cs)
	{
		if(newNode instanceof Processor)
		{
			Port[] ports = ((Processor)newNode).getPorts();
			for(int index = 0; index < ports.length; index++)
			{
				createAttributes(ports[index]);
			}
		}
		if (newNode instanceof DataConstraint)
		{
			DataConstraint constraint = (DataConstraint) newNode;
			cs.connect(constraint, constraint.getSource(), constraint.getSink());
			edgeAdded(constraint.getSource(), constraint);
			edgeAdded(constraint.getSink(), constraint);
		}
		else if (newNode instanceof ConcurrencyConstraint)
		{
			ConcurrencyConstraint constraint = (ConcurrencyConstraint)newNode;
			ConcurrencyPort sourcePort = getConcurrencyPort(constraint.getControllingProcessor());
			ConcurrencyPort targetPort = getConcurrencyPort(constraint.getTargetProcessor());
			sourcePort.addConstraint(constraint);
			targetPort.addConstraint(constraint);			
			cs.connect(constraint, sourcePort, targetPort);			
		}
		else if (newNode instanceof DummyProcessor)
		{
			DummyProcessor processor = (DummyProcessor)newNode;
			AttributeMap map = createAttributes(processor.getPort());
			map.put(PARENT_PROCESSOR, processor);
		}
		roots.add(newNode);
		return createAttributes(newNode);
	}
	
	void nodeRemoved(Object node)
	{
		if (node instanceof Processor)
		{
			Port[] ports = ((Processor)node).getPorts();
			for(int index = 0; index < ports.length; index++)
			{
				attributes.remove(ports[index]);
			}
		}
		else if (node instanceof DataConstraint)
		{
			DataConstraint constraint = (DataConstraint) node;
			edgeRemoved(constraint.getSource(), constraint);
			edgeRemoved(constraint.getSink(), constraint);
		}
		else if (node instanceof ConcurrencyConstraint)
		{
			ConcurrencyConstraint constraint = (ConcurrencyConstraint)node;
			getConcurrencyPort(constraint.getControllingProcessor()).removeConstraint(constraint);
			getConcurrencyPort(constraint.getTargetProcessor()).removeConstraint(constraint);
		}		
		else if (node instanceof DummyProcessor)
		{
			attributes.remove(((DummyProcessor) node).getPort());
		}
		roots.remove(node);
	}
	
	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public void graphChanged(GraphModelEvent event)
	{
		System.out.println("Graph changed");
		if(event.getChange() instanceof GraphModelEvent.ExecutableGraphChange)
		{
			((GraphModelEvent.ExecutableGraphChange)event.getChange()).execute();
		}
		fireGraphChangedEvent(event);
	}
}