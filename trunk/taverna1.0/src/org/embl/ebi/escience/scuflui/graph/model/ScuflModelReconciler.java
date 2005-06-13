/*
 * Created on Dec 13, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.embl.ebi.escience.scufl.MinorScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.jgraph.event.GraphModelEvent;

/**
 * The reconciler listens to scufl model changes, it manages the received change notifications in a
 * queue folding neighboring or overlapping changes together. Then passes on the changes after
 * having waited for further changes for the configured duration of time.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.5 $
 */
public class ScuflModelReconciler implements ScuflModelEventListener
{
	// Wait this for (milliseconds) to see if there are any more events
	// Too high and the ui becomes unresponsive, too low and can currently cause problems
	private static final long waitTime = 500;

	ScuflGraphModel model;
	ScuflGraphModelChange changes;
	ArrayList events = new ArrayList();
	private Thread reconciler = new Thread()
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
					changes.calculateChanges(event);
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
		if (!(event instanceof MinorScuflModelEvent))
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
	}

	/**
	 * 
	 */
	public void detachFromModel()
	{
		model.getModel().removeListener(this);
		model = null;
		synchronized (reconciler)
		{
			reconciler.notify();
		}
	}
}