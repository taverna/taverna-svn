/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModelAddEvent;
import org.embl.ebi.escience.scufl.ScuflModelRemoveEvent;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 */
public class ScuflGraphModelChange implements GraphModelChange,
		GraphModelEvent.ExecutableGraphChange
{
	private ScuflGraphModel model;
	private Collection inserted = new ArrayList();
	private Collection removed = new ArrayList();
	private ConnectionSet connectionSet;
	private Map attributes = new HashMap();
	private Map cellViews = new HashMap();
	private Collection changed = new HashSet();
	private Object[] context;

	/**
	 * @param model
	 */
	public ScuflGraphModelChange(ScuflGraphModel model)
	{
		super();
		this.model = model;
	}

	private List getRoots(ScuflModel scuflModel)
	{
		List newRoots = new ArrayList();
		newRoots.addAll(Arrays.asList(scuflModel.getProcessors()));
		Processor processor = scuflModel.getWorkflowSinkProcessor();
		if(processor.getPorts().length != 0)
		{
			newRoots.add(processor);
		}
		processor = scuflModel.getWorkflowSourceProcessor();
		if(processor.getPorts().length != 0)
		{
			newRoots.add(processor);
		}
		newRoots.addAll(Arrays.asList(scuflModel.getConcurrencyConstraints()));
		newRoots.addAll(Arrays.asList(scuflModel.getDataConstraints()));
		return newRoots;
	}

	/**
	 * @param event
	 */
	public void calculateChanges(ScuflModelEvent event)
	{
		// TODO Also add new ports/children to inserted/removed		
		Object source = event.getSource();
		if(event instanceof ScuflModelRemoveEvent)
		{
			Object removedObject = ((ScuflModelRemoveEvent)event).getRemovedObject();
			if(!model.isPort(removedObject))
			{
				removed.add(removedObject);
				if(removedObject instanceof Port && ((Port)removedObject).getProcessor().getPorts().length == 0)
				{
					removed.add(((Port)removedObject).getProcessor());
				}
			}
		}
		else if(event instanceof ScuflModelAddEvent)
		{
			Object addedObject = ((ScuflModelAddEvent)event).getAddedObject();
			if(!model.isPort(addedObject))
			{
				inserted.add(addedObject);
				if(addedObject instanceof Port && model.contains(((Port)addedObject).getProcessor()))
				{
					inserted.add(((Port)addedObject).getProcessor());
				}				
			}
		}
		else
		{
			if (source instanceof ScuflModel)
			{
				List newRoots = getRoots((ScuflModel) source);
				List roots = model.getRoots();
				inserted.addAll(difference(roots, newRoots));
				removed.addAll(difference(newRoots, roots));
	
				// if(connectionSet != null)
				// {
				// changed.addAll(connectionSet.getChangedEdges());
				// }
			}
			else if(source instanceof Processor)
			{
				//TODO Change scufl event model to send actual add events
				if(model.getRoots().contains(source))
				{
					changed.add(source);
					ScuflModel scuflModel = model.getModel();
					if(source == scuflModel.getWorkflowSinkProcessor() || source == scuflModel.getWorkflowSourceProcessor())
					{
						//TODO Check removed/added ports!
					}
					else
					{
						Processor processor = (Processor)source;
						Map attrs = model.getAttributes(processor);
						String name = (String)GraphConstants.getValue(attrs);
						if(!name.equals(processor.getName()))
						{
							Map procAttr = (Map)attributes.get(processor);
							if(procAttr == null)
							{
								procAttr = new HashMap();
								attributes.put(processor, procAttr);
							}
							GraphConstants.setValue(procAttr, processor.getName());
						}
					}
				}
				else
				{
					inserted.add(source);
				}
			}
		}
		//return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getInserted()
	 */
	public Object[] getInserted()
	{
		return inserted.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getRemoved()
	 */
	public Object[] getRemoved()
	{
		return removed.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousAttributes()
	 */
	public Map getPreviousAttributes()
	{
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousConnectionSet()
	 */
	public ConnectionSet getPreviousConnectionSet()
	{
		return connectionSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousParentMap()
	 */
	public ParentMap getPreviousParentMap()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#putViews(org.jgraph.graph.GraphLayoutCache,
	 *      org.jgraph.graph.CellView[])
	 */
	public void putViews(GraphLayoutCache view, CellView[] views)
	{
		if (view != null && views != null)
			cellViews.put(view, views);
	}

	public CellView[] getViews(GraphLayoutCache view)
	{
		return (CellView[]) cellViews.get(view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getSource()
	 */
	public Object getSource()
	{
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getChanged()
	 */
	public Object[] getChanged()
	{
		return changed.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getAttributes()
	 */
	public Map getAttributes()
	{
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getContext()
	 */
	public Object[] getContext()
	{
		return context;
	}

	/**
	 * @return <code>true</code> if the model has changed, <code>false</code>
	 *         otherwise.
	 */
	public boolean hasChanges()
	{
		return !inserted.isEmpty() || !removed.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.ExecutableGraphChange#execute()
	 */
	public void execute()
	{
		model.updateAttributes(attributes);

		Iterator insertIterator = inserted.iterator();
		ConnectionSet cs = new ConnectionSet();
		while (insertIterator.hasNext())
		{
			Object insertedObject = insertIterator.next();
			attributes.put(insertedObject, model.nodeAdded(insertedObject, cs));
		}

		Iterator removeIterator = removed.iterator();
		while (removeIterator.hasNext())
		{
			Object removedObject = removeIterator.next();
			model.nodeRemoved(removedObject);
		}
	}

	private Collection difference(Collection collection1, Collection collection2)
	{
		Collection result = new ArrayList();
		Iterator iterator = collection2.iterator();
		while (iterator.hasNext())
		{
			Object object = iterator.next();
			if (!collection1.contains(object))
			{
				result.add(object);
			}
		}
		return result;
	}
}