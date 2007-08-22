package net.sf.taverna.t2.workflowmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Edit which contains an ordered list of child edits. Child
 * edits are applied collectively and in order, any failure in any child edit
 * causes an undo of previously applied children and a propogation of the edit
 * exception.
 * 
 * @author Tom Oinn
 * 
 */
public class CompoundEdit implements Edit<Object> {

	private final transient List<Edit<?>> childEdits;

	private transient boolean applied = false;

	/**
	 * Create a new compound edit with no existing Edit objects.
	 * 
	 */
	public CompoundEdit() {
		this.childEdits = new ArrayList<Edit<?>>();
	}

	/**
	 * Create a new compound edit with the specified edits as children.
	 */
	public CompoundEdit(List<Edit<?>> edits) {
		this.childEdits = edits;
	}

	/**
	 * Attempts to call the doEdit method of all child edits. If any of those
	 * children throws an EditException any successful edits are rolled back and
	 * the exception is rethrown as the cause of a new EditException from the
	 * CompoundEdit
	 */
	public synchronized Object doEdit() throws EditException {
		if (isApplied()) {
			throw new EditException("Cannot apply an edit more than once!");
		}
		List<Edit<?>> doneEdits = new ArrayList<Edit<?>>();
		try {
			for (Edit<?> edit : childEdits) {
				edit.doEdit();
				// Insert the done edit at position 0 in the list so we can
				// iterate over the list in the normal order if we need to
				// rollback, this ensures that the most recent edit is first.
				doneEdits.add(0, edit);
			}
			applied = true;
		} catch (EditException ee) {
			for (Edit<?> undoMe : doneEdits) {
				undoMe.undo();
			}
			applied = false;
			throw new EditException("Failed child of compound edit", ee);
		}
		return null;
	}

	/**
	 * There is no explicit subject for a compound edit, so this method always
	 * returns null.
	 */
	public Object getSubject() {
		return null;
	}

	/**
	 * Rolls back all child edits in reverse order
	 */
	public synchronized void undo() {
		for (int i = (childEdits.size() - 1); i >= 0; i--) {
			// Undo child edits in reverse order
			childEdits.get(i).undo();
		}
		applied = false;
	}

	public boolean isApplied() {
		return applied;
	}

}
