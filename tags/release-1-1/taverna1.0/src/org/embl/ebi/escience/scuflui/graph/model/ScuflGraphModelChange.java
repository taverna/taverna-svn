/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelAddEvent;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelRemoveEvent;
import org.embl.ebi.escience.scufl.ScuflModelRenameEvent;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.10 $
 */
public class ScuflGraphModelChange implements GraphModelChange
{
	private ScuflGraphModel model;
	private Collection added = new HashSet();
	private Collection removed = new HashSet();
	private ConnectionSet connectionSet;
	private Map attributes = new HashMap();
	private Map cellViews = new HashMap();
	private Collection changed = new HashSet();
	private Object[] context;
	private List newRoots;

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
		Object source = event.getSource();
		newRoots = getRoots(model.getModel());		
		if (event instanceof ScuflModelRemoveEvent)
		{
			Object removedObject = ((ScuflModelRemoveEvent) event).getRemovedObject();
			if (!model.isPort(removedObject) && removedObject instanceof Port
					&& ((Port) removedObject).getProcessor().getPorts().length == 0)
			{
				removedObject = model.getParent(removedObject);
			}
			addNode(removed,removedObject);
			Object parent = model.getParent(removedObject);
			if(parent != null)
			{
				changed.add(parent);
			}				
		}
		else if (event instanceof ScuflModelAddEvent)
		{
			Object addedObject = ((ScuflModelAddEvent) event).getAddedObject();
			if (!model.isPort(addedObject) && addedObject instanceof Port
					&& !model.contains(((Port) addedObject).getProcessor()))
			{
				addedObject = model.getParent(addedObject);
			}
			addNode(added, addedObject);
			Object parent = model.getParent(addedObject);
			if(parent != null)
			{
				changed.add(parent);
			}
		}
		else if(event instanceof ScuflModelRenameEvent)
		{
			if (model.contains(source))
			{
				Map attrs = (Map) attributes.get(source);
				if (attrs == null)
				{
					attrs = new HashMap();
					attributes.put(source, attrs);
				}
				String newName = ((Processor)source).getName();
				assert newName != null: source;
				assert !newName.equals(((ScuflModelRenameEvent)event).getOldName()): source;
				GraphConstants.setValue(attrs, newName);
				GraphConstants.setResize(attrs, true);
				GraphConstants.setBounds(attrs, new Rectangle2D.Float());
				changed.add(source);				
			}
		}
		else
		{
			if (source instanceof ScuflModel)
			{
				List roots = model.roots;
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
		return !added.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.ExecutableGraphChange#execute()
	 */
	public void execute()
	{
		model.roots = newRoots;
		model.updateAttributes(attributes);

		changed.addAll(added);
		
		Iterator addedIterator = added.iterator();
		ConnectionSet cs = new ConnectionSet();
		while (addedIterator.hasNext())
		{
			Object addedObject = addedIterator.next();
			attributes.put(addedObject, model.addNode(addedObject, cs));
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

	public ConnectionSet getConnectionSet()
	{
		return null;
	}

	public ParentMap getParentMap()
	{
		return null;
	}
}