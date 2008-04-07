/*
 * Created on Dec 13, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.jgraph.event.GraphModelEvent;

/**
 * The reconciler listens to scufl model changes, it manages the received change notifications in a
 * queue folding neighboring or overlapping changes together. Then passes on the changes after
 * having waited for further changes for the configured duration of time.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.6 $
 */
public class ScuflModelReconciler implements ScuflModelEventListener
{
	private static final long waitTime = 100;

	ScuflGraphModel model;
	ScuflGraphModelChange changes;
	ArrayList events = new ArrayList();
	private Thread reconciler = new Thread("Reconcile ScuflModel")
	{
		public void run()
		{
			while (true)
			{
				while (events.isEmpty() && model != null)
				{
					try
					{
						synchronized (this)
						{
							wait();
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				if (model == null)
				{
					return;
				}

				changes = new ScuflGraphModelChange(model);
				while (!events.isEmpty())
				{
					ScuflModelEvent event;
					synchronized (events)
					{
						event = (ScuflModelEvent) events.remove(0);
					}
					changes.addChanges(event);
					try
					{
						sleep(waitTime);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				if (changes.hasChanges())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							model.graphChanged(new GraphModelEvent(model, changes));
						}
					});
				}
			}
		}
	};

	/**
	 * @param model
	 */
	public ScuflModelReconciler(ScuflGraphModel model)
	{
		this.model = model;
		model.getModel().addListener(this);
		events.add(new ScuflModelEvent(model.getModel(), "Woo"));
		reconciler.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scufl.ScuflModelEventListener#receiveModelEvent(org.embl.ebi.escience.scufl.ScuflModelEvent)
	 */
	public void receiveModelEvent(ScuflModelEvent event)
	{
			synchronized (events)
			{
				events.add(event);
			}
			synchronized (reconciler)
			{
				reconciler.notify();
			}
	}

	/**
	 * 
	 */
	public synchronized void detachFromModel()
	{
		if (model != null) {
			model.getModel().removeListener(this);
			model = null;
		}
		synchronized (reconciler)
		{
			reconciler.notify();
		}
	}
}
