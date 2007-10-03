package org.embl.ebi.escience.scuflui.shared;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet.ScuflModelSetListener;

/**
 * Detect changes to open workflows since last save to disk.
 * <p>
 * Use hasChanged() to check if the workflow has been changed 
 * since the last save. Use syncedWithFile() to notify that
 * the workflow has been saved.
 * 
 * @author Stian Soiland
 *
 */
public class WorkflowChanges {

	static WorkflowChanges instance = null;
	
	Set<ScuflModel> changedWorkflows = new HashSet<ScuflModel>();

	Map<ScuflModel, File> filenames = new HashMap<ScuflModel, File>();

	Map<ScuflModel, ChangedModelListener> listeners = new HashMap<ScuflModel, ChangedModelListener>();

	RegisterModelListener registerListener = new RegisterModelListener();
	
	/**
	 * Get the singleton WorkflowChanges instance.
	 * 
	 * @return The WorkflowChanges instance.
	 */
	public static WorkflowChanges getInstance() {
		if (instance == null) {
			instance = new WorkflowChanges();
		}
		return instance;
	}
	
	/**
	 * Private constructor, use singleton pattern getInstance() instead.
	 * <p>
	 * Register as a listener with the ScuflModelSet.
	 * @param workbench TODO
	 */
	private WorkflowChanges() {
		ScuflModelSet.getInstance().addListener(registerListener);
	}
	
	/**
	 * Unregister as listener with ScuflModelSet and known ScuflModels
	 */
	public void destroy() {
		ScuflModelSet.getInstance().removeListener(registerListener);
		reset();
	}	

	/**
	 * Unregister as listener with known ScuflModels.
	 */
	private void reset() {
		// Remove existing listeners
		for (Entry<ScuflModel, ChangedModelListener> entry : listeners.entrySet()) {
			entry.getKey().removeListener(entry.getValue());
		}
		listeners.clear();
		filenames.clear();
		changedWorkflows.clear();
	}

	/**
	 * Return true if workflow has been changed since creation or last save,
	 * otherwise false.
	 * 
	 * @param workflow that 
	 * @return true if workflow has been changed
	 */
	public boolean hasChanged(ScuflModel workflow) {
		return changedWorkflows.contains(workflow);
	}

	/**
	 * Mark the workflow as synchronized with source. Concurrent calls to
	 * hasChanged() will return false until the workflow model is changed.
	 * 
	 * @param workflow ScuflModel that has been synchronized
	 */
	public void synced(ScuflModel workflow) {
		changedWorkflows.remove(workflow);
	}
	
	/**
	 * Mark the workflow as synchronized by saving to given file. 
	 * Concurrent calls to hasChanged() will return false until 
	 * the workflow model is changed. Calls to lastFilename() will 
	 * return the given file unless the workflow is later syncedWithFile()
	 * with some other file.
	 * 
	 * @param workflow ScuflModel that has been synchronized
	 * @param file File where the workflow has been stored
	 */
	public void syncedWithFile(ScuflModel workflow, File file) {
		filenames.put(workflow, file);
		synced(workflow);
	}

	/**
	 * Find the latest filename to which the workflow was saved, as
	 * notified with syncedWithFile().
	 * <p>
	 * Note: This method does not guarantee that the actual file has not
	 * been overwritten by some other process or workflow save since the
	 * last save.
	 * 
	 * @param workflow ScuflModel that has been previously saved
	 * @return File of the last saved
	 */
	public File lastFilename(ScuflModel workflow) {
		// FIXME: provide date of when last written, compare with 
		// file.lastModified()
		return filenames.get(workflow);
	}

	/**
	 * Add listeners for newly created/loaded models as registered with 
	 * ScuflModelSet. Remove listeners on model removal.
	 * 
	 * @author Stian Soiland
	 *
	 */
	private class RegisterModelListener implements ScuflModelSetListener {
	
		public void modelAdded(final ScuflModel workflow) {
			ChangedModelListener listener = new ChangedModelListener(workflow);
			listeners.put(workflow, listener);
			workflow.addListener(listener);
			synced(workflow); // initially unchanged
		}

		public void modelRemoved(ScuflModel workflow) {
			ChangedModelListener listener = listeners.get(workflow);
			if (listener != null) {
				workflow.removeListener(listener);
			}
			listeners.remove(workflow);
			filenames.remove(workflow);
		}
	}

	/**
	 * Listen for workflow events, mark as changed (ignoring the
	 * event type).
	 * 
	 * @author Stian Soiland
	 *
	 */
	private class ChangedModelListener implements ScuflModelEventListener {
		private ScuflModel workflow;

		private ChangedModelListener(ScuflModel workflow) {
			this.workflow = workflow;
		}

		public void receiveModelEvent(ScuflModelEvent event) {
			changedWorkflows.add(workflow);
		}
	}
}
