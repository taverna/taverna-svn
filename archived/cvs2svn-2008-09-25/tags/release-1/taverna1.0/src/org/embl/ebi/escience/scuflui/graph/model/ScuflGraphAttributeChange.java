/*
 * Created on Dec 13, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.Map;

import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class ScuflGraphAttributeChange implements GraphModelChange
{
	private Map attributes;

	/**
	 * @param attributes
	 */
	public ScuflGraphAttributeChange(Map attributes)
	{
		super();
		this.attributes = attributes;
	}
	
	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getInserted()
	 */
	public Object[] getInserted()
	{
		// TODO
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getRemoved()
	 */
	public Object[] getRemoved()
	{
		// TODO
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousAttributes()
	 */
	public Map getPreviousAttributes()
	{
		return attributes;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousConnectionSet()
	 */
	public ConnectionSet getPreviousConnectionSet()
	{
		// TODO Auto-generated method stub
		return null;
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
	public void putViews(GraphLayoutCache view, CellView[] cellViews)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getViews(org.jgraph.graph.GraphLayoutCache)
	 */
	public CellView[] getViews(GraphLayoutCache view)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getSource()
	 */
	public Object getSource()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getChanged()
	 */
	public Object[] getChanged()
	{
		return attributes.keySet().toArray();
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getAttributes()
	 */
	public Map getAttributes()
	{
		return attributes;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getContext()
	 */
	public Object[] getContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
