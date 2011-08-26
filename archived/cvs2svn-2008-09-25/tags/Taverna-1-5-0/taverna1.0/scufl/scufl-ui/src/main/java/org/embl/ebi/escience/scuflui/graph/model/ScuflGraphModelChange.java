/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
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
 * @version $Revision: 1.2 $
 */
public class ScuflGraphModelChange implements GraphModelChange
{
	ScuflGraphModel model;
	private ConnectionSet connectionSet;
	private Collection inserted = new HashSet();
	private Collection removed = new HashSet();
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
		if (model.isShowingBoring())
		{
			newRoots.addAll(Arrays.asList(scuflModel.getProcessors()));
			newRoots.addAll(Arrays.asList(scuflModel.getConcurrencyConstraints()));
			newRoots.addAll(Arrays.asList(scuflModel.getDataConstraints()));
		}
		else
		{
			ArrayList roots = new ArrayList();
			roots.addAll(Arrays.asList(scuflModel.getProcessors()));
			roots.addAll(Arrays.asList(scuflModel.getConcurrencyConstraints()));
			roots.addAll(Arrays.asList(scuflModel.getDataConstraints()));
			Iterator iterator = roots.iterator();
			while (iterator.hasNext())
			{
				Object node = iterator.next();
				if (!isBoring(node))
				{
					newRoots.add(node);
				}
			}
		}

		return newRoots;
	}

	private void addNode(Collection collection, Object node)
	{
		collection.add(node);
		if (node instanceof Processor)
		{
			Port[] ports = ((Processor) node).getPorts();
			for (int index = 0; index < ports.length; index++)
			{
				collection.add(ports[index]);
			}
		}
	}

	/**
	 * @param event
	 */
	public void addChanges(ScuflModelEvent event)
	{
		Object source = event.getSource();
		if (event instanceof ScuflModelRemoveEvent)
		{
			Object removedObject = ((ScuflModelRemoveEvent) event).getRemovedObject();
			if (removedObject instanceof Collection)
			{
				Iterator removes = ((Collection) removedObject).iterator();
				while (removes.hasNext())
				{
					addRemovedObject(removes.next());
				}
			}
			else if (removedObject instanceof Object[])
			{
				Object[] removes = (Object[]) removedObject;
				for (int index = 0; index < removes.length; index++)
				{
					addRemovedObject(removes[index]);
				}
			}
			else
			{
				addRemovedObject(removedObject);
			}
		}
		else if (event instanceof ScuflModelAddEvent)
		{
			Object insertedObject = ((ScuflModelAddEvent) event).getAddedObject();
			if (insertedObject instanceof Collection)
			{
				Iterator inserts = ((Collection) insertedObject).iterator();
				while (inserts.hasNext())
				{
					addInsertedObject(inserts.next());
				}
			}
			else if (insertedObject instanceof Object[])
			{
				Object[] inserts = (Object[]) insertedObject;
				for (int index = 0; index < inserts.length; index++)
				{
					addInsertedObject(inserts[index]);
				}
			}
			else
			{
				addInsertedObject(insertedObject);
			}

		}
		else if (event instanceof ScuflModelRenameEvent)
		{
			if (model.contains(source))
			{
				Map attrs = (Map) attributes.get(source);
				if (attrs == null)
				{
					attrs = new HashMap();
					attributes.put(source, attrs);
				}
				String newName = ((Processor) source).getName();
				assert newName != null : source;
				assert !newName.equals(((ScuflModelRenameEvent) event).getOldName()) : source;
				GraphConstants.setValue(attrs, newName);
				GraphConstants.setResize(attrs, true);
				GraphConstants.setBounds(attrs, GraphConstants.getBounds(model
						.getAttributes(source)));
				changed.add(source);
			}
		}
		else
		{
			if (source instanceof ScuflModel)
			{
				List roots = model.roots;
				List newRoots = getRoots(model.getModel());
				Iterator difference = difference(roots, newRoots).iterator();
				while (difference.hasNext())
				{
					addInsertedObject(difference.next());
				}
				difference = difference(newRoots, roots).iterator();
				while (difference.hasNext())
				{
					addRemovedObject(difference.next());
				}
			}
		}
	}

	private void addRemovedObject(Object removedObject)
	{
		if (!model.isPort(removedObject) && removedObject instanceof Port
				&& ((Port) removedObject).getProcessor().getPorts().length == 0)
		{
			removedObject = model.getParent(removedObject);
		}
		// if (model.isEdge(removedObject))
		// {
		// removedEdges.add(removedObject);
		// }
		addNode(removed, removedObject);
		Object parent = model.getParent(removedObject);
		if (parent != null)
		{
			changed.add(parent);
		}
	}

	private void addInsertedObject(Object insertedObject)
	{
		if (!model.isPort(insertedObject) && insertedObject instanceof Port
				&& !model.contains(((Port) insertedObject).getProcessor()))
		{
			insertedObject = model.getParent(insertedObject);
		}
		// if (model.isEdge(insertedObject))
		// {
		// insertedEdges.add(insertedObject);
		// }
		addNode(inserted, insertedObject);
		Object parent = model.getParent(insertedObject);
		if (parent != null)
		{
			changed.add(parent);
		}
	}

	public boolean isBoring(Object object)
	{
		if (object instanceof Processor)
		{
			return ((Processor) object).isBoring();
		}
		else if (object instanceof DataConstraint)
		{
			DataConstraint constraint = (DataConstraint) object;
			return constraint.getSource().getProcessor().isBoring()
					|| constraint.getSink().getProcessor().isBoring();
		}
		else if (object instanceof ConcurrencyConstraint)
		{
			ConcurrencyConstraint constraint = (ConcurrencyConstraint) object;
			return constraint.getControllingProcessor().isBoring()
					|| constraint.getTargetProcessor().isBoring();
		}
		return false;
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
	 * @return <code>true</code> if the model has changed, <code>false</code> otherwise.
	 */
	public boolean hasChanges()
	{
		return !inserted.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelEvent.ExecutableGraphChange#execute()
	 */
	public void execute()
	{
		model.updateAttributes(attributes);

		changed.addAll(inserted);

		ArrayList insertedList = new ArrayList(inserted);
		Collections.sort(insertedList, new Comparator()
		{

			public int compare(Object o1, Object o2)
			{
				int o1value = Integer.MAX_VALUE;
				int o2value = Integer.MAX_VALUE;
				if (!model.isEdge(o1))
				{
					Object parent = o1;
					o1value = 0;
					while (parent != null)
					{
						o1value++;
						parent = model.getParent(parent);
					}
				}

				if (!model.isEdge(o2))
				{
					Object parent = o2;
					o2value = 0;
					while (parent != null)
					{
						o2value++;
						parent = model.getParent(parent);
					}
				}

				return o1value - o2value;
			}

		});
		Iterator insertedIterator = insertedList.iterator();

		ConnectionSet cs = new ConnectionSet();
		while (insertedIterator.hasNext())
		{
			Object addedObject = insertedIterator.next();
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