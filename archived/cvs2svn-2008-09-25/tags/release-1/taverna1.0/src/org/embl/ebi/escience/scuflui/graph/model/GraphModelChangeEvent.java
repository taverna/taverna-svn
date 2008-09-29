/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class GraphModelChangeEvent implements GraphModelChange
{
	private Object[] inserted;
	private Object[] removed;
	private ConnectionSet previousConnectionSet;
	private Map previousAttributes;
	private Map cellViews = new HashMap();
	private Object[] changed;
	private Object[] context;
	private GraphModel model;

	/**
	 * @param model
	 * @param inserted
	 * @param removed
	 * @param cs
	 * @param attributes
	 */
	public GraphModelChangeEvent(GraphModel model, Object[] inserted, Object[] removed, ConnectionSet cs, Map attributes)
	{
		super();
		this.inserted = inserted;
		this.removed = removed;
		this.previousConnectionSet = cs;
		this.previousAttributes = attributes;
		
		Collection changeList = new HashSet();
		if(attributes != null)
		{
			changeList.addAll(attributes.keySet());
		}
		if(cs != null)
		{
			changeList.addAll(cs.getChangedEdges());
		}
		changed = changeList.toArray();
		
		Collection ctx = DefaultGraphModel.getEdges(model, changed);
		context = ctx.toArray();
	}
	
	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getInserted()
	 */
	public Object[] getInserted()
	{
		return inserted;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getRemoved()
	 */
	public Object[] getRemoved()
	{
		return removed;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousAttributes()
	 */
	public Map getPreviousAttributes()
	{
		return previousAttributes;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousConnectionSet()
	 */
	public ConnectionSet getPreviousConnectionSet()
	{
		return previousConnectionSet;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousParentMap()
	 */
	public ParentMap getPreviousParentMap()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#putViews(org.jgraph.graph.GraphLayoutCache, org.jgraph.graph.CellView[])
	 */
	public void putViews(GraphLayoutCache view, CellView[] views) {
		if (view != null && views != null)
			cellViews.put(view, views);
	}

	public CellView[] getViews(GraphLayoutCache view) {
		return (CellView[]) cellViews.get(view);
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getSource()
	 */
	public Object getSource()
	{
		return model;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getChanged()
	 */
	public Object[] getChanged()
	{
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getAttributes()
	 */
	public Map getAttributes()
	{
		return previousAttributes;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getContext()
	 */
	public Object[] getContext()
	{
		return context;
	}

}
