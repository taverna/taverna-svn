package net.sf.taverna.t2.workbench.edits.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

/**
 * Implementation of {@link EditManager}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class EditManagerImpl extends EditManager {

	private MultiCaster<EditManagerEvent> multiCaster = new MultiCaster<EditManagerEvent>(
			this);

	protected Map<Dataflow, DataflowEdits> editsForDataflow = new HashMap<Dataflow, DataflowEdits>();

	/**
	 * {@inheritDoc}
	 */
	public void addObserver(Observer<EditManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	@Override
	public boolean canRedoDataflowEdit(Dataflow dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		return edits.canRedo();
	}

	@Override
	public boolean canUndoDataflowEdit(Dataflow dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		return edits.canUndo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doDataflowEdit(Dataflow dataflow, Edit<?> edit)
			throws EditException {
		// We do the edit before we notify the observers
		DataflowEdits edits = getEditsForDataflow(dataflow);
		synchronized (edits) {
			// Make sure the edits are in the order they were performed
			edit.doEdit();
			edits.addEdit(edit);
		}
		multiCaster.notify(new DataflowEditEvent(dataflow, edit));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Edits getEdits() {
		return new EditsImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Observer<EditManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void redoDataflowEdit(Dataflow dataflow) throws EditException {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		Edit<?> edit;
		synchronized (edits) {
			if (!edits.canRedo()) {
				return;
			}
			edit = edits.getLastUndo();
			edit.doEdit();
			edits.addRedo(edit);
		}
		multiCaster.notify(new DataFlowRedoEvent(dataflow, edit));

	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObserver(Observer<EditManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void undoDataflowEdit(Dataflow dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		Edit<?> edit;
		synchronized (edits) {
			if (!edits.canUndo()) {
				return;
			}
			edit = edits.getLastEdit();
			edit.undo();
			edits.addUndo(edit);
		}
		multiCaster.notify(new DataFlowUndoEvent(dataflow, edit));
	}

	protected synchronized DataflowEdits getEditsForDataflow(Dataflow dataflow) {
		DataflowEdits edits = editsForDataflow.get(dataflow);
		if (edits == null) {
			edits = new DataflowEdits();
			editsForDataflow.put(dataflow, edits);
		}
		return edits;
	}

	/**
	 * A set of edits and undoes for a {@link Dataflow}
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public class DataflowEdits {
		/*
		 * List of edits that have been performed and can be undone.
		 */
		private List<Edit<?>> edits = new ArrayList<Edit<?>>();
		/*
		 * List of edits that have been undone and can be redone
		 */
		private List<Edit<?>> undoes = new ArrayList<Edit<?>>();

		/**
		 * Add an {@link Edit} that has been done by the EditManager.
		 * <p>
		 * This can later be retrieved using {@link #getLastEdit()}. After
		 * calling this {@link #canRedo()} will be false.
		 * 
		 * @param edit
		 *            {@link Edit} that has been undone
		 */
		public synchronized void addEdit(Edit<?> edit) {
			addEditOrRedo(edit, false);
		}

		/**
		 * Add an {@link Edit} that has been redone by the EditManager.
		 * <p>
		 * The {@link Edit} must be the same as the last undo returned through
		 * {@link #getLastUndo()}.
		 * <p>
		 * This method works like {@link #addEdit(Edit)} except that instead of
		 * removing all possible redoes, only the given {@link Edit} is removed.
		 * 
		 * @param edit
		 *            {@link Edit} that has been redone
		 */
		public synchronized void addRedo(Edit<?> edit) {
			addEditOrRedo(edit, true);
		}

		/**
		 * Add an {@link Edit} that has been undone by the EditManager.
		 * <p>
		 * After calling this method {@link #canRedo()} will be true, and the
		 * edit can be retrieved using {@link #getLastUndo()}.
		 * </p>
		 * <p>
		 * The {@link Edit} must be the last edit returned from
		 * {@link #getLastEdit()}, after calling this method
		 * {@link #getLastEdit()} will return the previous edit or
		 * {@link #canUndo()} will be false if there are no more edits.
		 * 
		 * @param edit
		 *            {@link Edit} that has been undone
		 */
		public synchronized void addUndo(Edit<?> edit) {
			int lastIndex = edits.size() - 1;
			if (lastIndex < 0 || !edits.get(lastIndex).equals(edit)) {
				throw new IllegalArgumentException("Can't undo unknown edit "
						+ edit);
			}
			undoes.add(edit);
			edits.remove(lastIndex);
		}

		/**
		 * True if there are undone events that can be redone.
		 * 
		 * @return
		 */
		public boolean canRedo() {
			return !undoes.isEmpty();
		}

		/**
		 * True if there are edits that can be undone and later added with
		 * {@link #addUndo(Edit)}.
		 * 
		 * @return
		 */
		public boolean canUndo() {
			return !edits.isEmpty();
		}

		/**
		 * Get the last edit that can be undone. This edit was the last one to
		 * be added with {@link #addEdit(Edit)} or {@link #addRedo(Edit)}.
		 * 
		 * @return The last added {@link Edit}
		 * @throws IllegalStateException
		 *             If there are no more edits (Check with {@link #canUndo()}
		 *             first)
		 * 
		 */
		public synchronized Edit<?> getLastEdit() throws IllegalStateException {
			if (edits.isEmpty()) {
				throw new IllegalStateException("No more edits");
			}
			int lastEdit = edits.size() - 1;
			return edits.get(lastEdit);
		}

		/**
		 * Get the last edit that can be redone. This edit was the last one to
		 * be added with {@link #addUndo(Edit)}.
		 * 
		 * @return The last undone {@link Edit}
		 * @throws IllegalStateException
		 *             If there are no more edits (Check with {@link #canRedo()}
		 *             first)
		 * 
		 */
		public synchronized Edit<?> getLastUndo() throws IllegalStateException {
			if (undoes.isEmpty()) {
				throw new IllegalStateException("No more undoes");
			}
			int lastUndo = undoes.size() - 1;
			return undoes.get(lastUndo);
		}

		protected void addEditOrRedo(Edit<?> edit, boolean isRedo) {
			edits.add(edit);
			if (undoes.isEmpty()) {
				return;
			}
			if (isRedo) {
				// It's a redo, remove only the last one
				int lastUndoIndex = undoes.size() - 1;
				Edit<?> lastUndo = undoes.get(lastUndoIndex);
				if (!edit.equals(lastUndo)) {
					throw new IllegalArgumentException(
							"Can only redo last undo");
				}
				undoes.remove(lastUndoIndex);
			} else {
				// It's a new edit, remove all redos
				undoes.clear();
			}
		}

	}

}
