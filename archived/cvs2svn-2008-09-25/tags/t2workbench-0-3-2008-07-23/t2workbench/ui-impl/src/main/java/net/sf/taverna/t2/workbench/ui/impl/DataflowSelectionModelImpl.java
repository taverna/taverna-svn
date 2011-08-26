package net.sf.taverna.t2.workbench.ui.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage.Type;

/**
 * Default implementation of a <code>DataflowSelectionModel</code>.
 * 
 * @author David Withers
 */
public class DataflowSelectionModelImpl implements DataflowSelectionModel {

	private MultiCaster<DataflowSelectionMessage> multiCaster;

	private Set<Object> selection = new HashSet<Object>();

	/**
	 * Constructs a new instance of DataflowSelectionModelImpl.
	 *
	 */
	public DataflowSelectionModelImpl() {
		multiCaster = new MultiCaster<DataflowSelectionMessage>(this);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.DataflowSelectionModel#addSelection(java.lang.Object)
	 */
	public void addSelection(Object element) {
		if (element != null) {
			if (!selection.contains(element)) {
				clearSelection();
				selection.add(element);
				multiCaster.notify(new DataflowSelectionMessage(Type.ADDED, element));
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.DataflowSelectionModel#clearSelection()
	 */
	public void clearSelection() {
		for (Object element : new HashSet<Object>(selection)) {
			removeSelection(element);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.DataflowSelectionModel#getSelection()
	 */
	public Set<Object> getSelection() {
		return new HashSet<Object>(selection);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.DataflowSelectionModel#removeSelection(java.lang.Object)
	 */
	public void removeSelection(Object element) {
		if (element != null && selection.remove(element)) {
			multiCaster
					.notify(new DataflowSelectionMessage(Type.REMOVED, element));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.DataflowSelectionModel#setSelection(java.util.Set)
	 */
	public void setSelection(Set<Object> elements) {
		if (elements == null) {
			clearSelection();
		} else {
			Set<Object> newSelection = new HashSet<Object>(elements);
			for (Object element : new HashSet<Object>(selection)) {
				if (!newSelection.remove(element)) {
					removeSelection(element);
				}
			}
			for (Object element : newSelection) {
				addSelection(element);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void addObserver(Observer<DataflowSelectionMessage> observer) {
		multiCaster.addObserver(observer);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#getObservers()
	 */
	public List<Observer<DataflowSelectionMessage>> getObservers() {
		return multiCaster.getObservers();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#removeObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void removeObserver(Observer<DataflowSelectionMessage> observer) {
		multiCaster.removeObserver(observer);
	}

}
