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
 * @version $Revision: 1.4 $
 */
public class ScuflGraphModelChange implements GraphModelChange,
		GraphModelEvent.ExecutableGraphChange
{
	private ScuflGraphModel model;
	private Collection added = new ArrayList();
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
		Processor processor = scuflModel.getWorkflowSinkProcessor();
		if (processor.getPorts().length != 0)
		{
			newRoots.add(processor);
		}
		processor = scuflModel.getWorkflowSourceProcessor();
		if (processor.getPorts().length != 0)
		{
			newRoots.add(processor);
		}
		newRoots.addAll(Arrays.asList(scuflModel.getProcessors()));
		newRoots.addAll(Arrays.asList(scuflModel.getConcurrencyConstraints()));
		newRoots.addAll(Arrays.asList(scuflModel.getDataConstraints()));
		return newRoots;
	}
	
	private void addNode(Collection collection, Object node)
	{
		collection.add(node);
		if(node instanceof Processor)
		{
			Port[] ports = ((Processor)node).getPorts();
			for(int index = 0; index < ports.length; index++)
			{
				collection.add(ports[index]);
			}				
		}
	}

	/**
	 * @param event
	 */
	public void calculateChanges(ScuflModelEvent event)
	{
		// TODO Also add new ports/children to inserted/removed
		Object source = event.getSource();
		if (event instanceof ScuflModelRemoveEvent)
		{
			Object removedObject = ((ScuflModelRemoveEvent) event).getRemovedObject();
			addNode(removed, removedObject);
			if (!model.isPort(removedObject) && removedObject instanceof Port
					&& ((Port) removedObject).getProcessor().getPorts().length == 0)
			{
				removed.add(((Port) removedObject).getProcessor());
			}
		}
		else if (event instanceof ScuflModelAddEvent)
		{
			Object addedObject = ((ScuflModelAddEvent) event).getAddedObject();
			addNode(added, addedObject);
			if (!model.isPort(addedObject) && addedObject instanceof Port
					&& model.contains(((Port) addedObject).getProcessor()))
			{
				added.add(((Port) addedObject).getProcessor());
			}
		}
		else
		{
			if (source instanceof ScuflModel)
			{
				List newRoots = getRoots((ScuflModel) source);
				List roots = model.getRoots();
				Iterator difference = difference(roots, newRoots).iterator();
				while(difference.hasNext())
				{
					addNode(added, difference.next());
				}
				difference = difference(newRoots, roots).iterator();
				while(difference.hasNext())
				{
					addNode(removed, difference.next());
				}
			}
			else if (source instanceof Processor)
			{
				// TODO Change scufl event model to send actual add events
				if (model.contains(source))
				{
					changed.add(source);
					ScuflModel scuflModel = model.getModel();
					if (source != scuflModel.getWorkflowSinkProcessor()
							&& source != scuflModel.getWorkflowSourceProcessor())
					{
						Processor processor = (Processor) source;
						Map attrs = model.getAttributes(processor);
						String name = (String) GraphConstants.getValue(attrs);
						if (!name.equals(processor.getName()))
						{
							Map procAttr = (Map) attributes.get(processor);
							if (procAttr == null)
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
					added.add(source);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getInserted()
	 */
	public Object[] getInserted()
	{
		return added.toArray();
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
		return !added.isEmpty() || !removed.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.ExecutableGraphChange#execute()
	 */
	public void execute()
	{
		model.updateAttributes(attributes);

		Iterator insertIterator = added.iterator();
		ConnectionSet cs = new ConnectionSet();
		while (insertIterator.hasNext())
		{
			Object insertedObject = insertIterator.next();
			attributes.put(insertedObject, model.addNode(insertedObject, cs));
		}

		Iterator removeIterator = removed.iterator();
		while (removeIterator.hasNext())
		{
			Object removedObject = removeIterator.next();
			model.removeNode(removedObject);
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