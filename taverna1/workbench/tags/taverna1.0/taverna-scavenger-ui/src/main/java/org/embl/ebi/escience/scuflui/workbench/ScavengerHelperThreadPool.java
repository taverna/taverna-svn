package org.embl.ebi.escience.scuflui.workbench;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * A thread pool that handles a collection of Scavenger threads (for
 * URLBasedScavengers) The pool ensures that there is a maximum number of
 * concurrent threads, and maintains a waiting list for those threads that
 * cannot yet be started. Each time getCompleted is called and the completed
 * Scavengers are returned, waiting threads are moved to the active list and
 * started.
 * 
 * The pool is complete when all completed Scavengers have been return through
 * getComplete, and isEmpty returns true
 * 
 * @author Stuart Owen
 * 
 */
public class ScavengerHelperThreadPool {

	private int MAX_THREADS = 5;

	private List<ScavengerHelperThread> waitingThreads = new ArrayList<ScavengerHelperThread>();

	private List<ScavengerHelperThread> activeThreads = new ArrayList<ScavengerHelperThread>();

	public void addThread(ScavengerHelperThread thread) {
		synchronized (activeThreads) {
			if (activeThreads.size() < MAX_THREADS) {
				thread.start();
				activeThreads.add(thread);
			} else {
				waitingThreads.add(thread);
			}
		}
	}

	/**
	 * Returns the number of threads waiting to start
	 * 
	 * @return
	 */
	public int waiting() {
		return waitingThreads.size();
	}

	/**
	 * Returns the total number of threads waiting to complete, or waiting to be
	 * removed via getCompleted. This will be zero when all threads have
	 * finished and have had their results retrieved.
	 * 
	 * @return
	 */
	public int remaining() {
		return activeThreads.size() + waitingThreads.size();
	}

	/**
	 * Adds a new scavenger helper, and creates a thread for it to load the
	 * default scavengers. The thread will either be started immediately, or
	 * placed on the waiting queue.
	 * 
	 * @param helper
	 */
	public void addScavengerHelper(ScavengerHelper helper) {
		ScavengerHelperThread thread = new ScavengerHelperDefaultsThread(helper);
		addThread(thread);
	}

	/**
	 * Scavengers over a ScuflModel for processors that are understood by the
	 * scavenger helper. The thread will either be started immediately, or
	 * placed on the waiting queue.
	 * 
	 * @param helper
	 * @param theModel
	 */
	public void addScavengerHelperForModel(ScavengerHelper helper,
			ScuflModel theModel) {
		ScavengerHelperThread thread = new ScavengerHelperForModelThread(
				helper, theModel);
		addThread(thread);
	}

	/**
	 * Returns a Set of Scavengers that have completed their initialisation
	 * 
	 * @return
	 */
	public Set<Scavenger> getCompleted() {
		List<ScavengerHelperThread> completedThreads = updateCompleted();
		Set<Scavenger> result = new HashSet<Scavenger>();
		for (ScavengerHelperThread thread : completedThreads) {
			result.addAll(thread.getScavengers());
		}
		return result;
	}

	/**
	 * Returns true if all threads have completed and have had their results
	 * retrieved via getCompleted
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return remaining() == 0;
	}

	private List<ScavengerHelperThread> updateCompleted() {
		List<ScavengerHelperThread> completedThreads = new ArrayList<ScavengerHelperThread>();
		synchronized (activeThreads) {
			for (ScavengerHelperThread thread : activeThreads) {
				if (thread.isComplete()) {
					completedThreads.add(thread);
				}
			}
			activeThreads.removeAll(completedThreads);
			moveFromWaitingList();
		}
		return completedThreads;
	}

	private void moveFromWaitingList() {
		while (activeThreads.size() < MAX_THREADS && !waitingThreads.isEmpty()) {
			ScavengerHelperThread thread = waitingThreads.get(0);
			waitingThreads.remove(thread);
			thread.start();
			activeThreads.add(thread);
		}
	}

	private abstract class ScavengerHelperThread extends Thread {
		protected ScavengerHelper scavengerHelper;

		protected boolean complete = false;

		protected Set<Scavenger> scavengers;

		public ScavengerHelperThread(String message) {
			super(message);
		}

		public Set<Scavenger> getScavengers() {
			return scavengers;
		}

		public boolean isComplete() {
			return complete;
		}
	};

	private class ScavengerHelperDefaultsThread extends ScavengerHelperThread {

		public ScavengerHelperDefaultsThread(ScavengerHelper scavengerHelper) {
			super("Scavanger initialisation");
			this.scavengerHelper = scavengerHelper;
		}

		public void run() {
			scavengers = scavengerHelper.getDefaults();
			complete = true;
		}

	}

	private class ScavengerHelperForModelThread extends ScavengerHelperThread {

		private ScuflModel theModel;

		public ScavengerHelperForModelThread(ScavengerHelper scavengerHelper,
				ScuflModel theModel) {
			super("Scavanger initialisation");
			this.scavengerHelper = scavengerHelper;
			this.theModel = theModel;
		}

		public void run() {
			scavengers = scavengerHelper.getFromModel(theModel);
			complete = true;
		}

	}
}
