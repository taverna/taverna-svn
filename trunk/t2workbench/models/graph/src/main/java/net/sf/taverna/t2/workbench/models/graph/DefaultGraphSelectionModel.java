package net.sf.taverna.t2.workbench.models.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.models.graph.GraphSelectionMessage.Type;

/**
 * Default implementation of a <code>GraphSelectionModel</code>.
 * 
 * @author David Withers
 */
/**
 * @author David Withers
 *
 */
public class DefaultGraphSelectionModel implements GraphSelectionModel {

	private MultiCaster<GraphSelectionMessage> multiCaster;

	private Set<GraphElement> selection = new HashSet<GraphElement>();

	/**
	 * Constructs a new instance of DefaultGraphSelectionModel.
	 *
	 */
	public DefaultGraphSelectionModel() {
		multiCaster = new MultiCaster<GraphSelectionMessage>(this);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphSelectionModel#addSelection(net.sf.taverna.t2.workbench.models.graph.GraphElement)
	 */
	public void addSelection(GraphElement element) {
		if (element != null && selection.add(element)) {
			multiCaster.notify(new GraphSelectionMessage(Type.ADDED, element));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphSelectionModel#clearSelection()
	 */
	public void clearSelection() {
		for (GraphElement element : new HashSet<GraphElement>(selection)) {
			removeSelection(element);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphSelectionModel#getSelection()
	 */
	public Set<GraphElement> getSelection() {
		return new HashSet<GraphElement>(selection);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphSelectionModel#removeSelection(net.sf.taverna.t2.workbench.models.graph.GraphElement)
	 */
	public void removeSelection(GraphElement element) {
		if (element != null && selection.remove(element)) {
			multiCaster
					.notify(new GraphSelectionMessage(Type.REMOVED, element));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphSelectionModel#setSelection(java.util.Set)
	 */
	public void setSelection(Set<GraphElement> elements) {
		if (elements == null) {
			clearSelection();
		} else {
			Set<GraphElement> newSelection = new HashSet<GraphElement>(elements);
			for (GraphElement element : new HashSet<GraphElement>(selection)) {
				if (!newSelection.remove(element)) {
					removeSelection(element);
				}
			}
			for (GraphElement element : newSelection) {
				addSelection(element);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void addObserver(Observer<GraphSelectionMessage> observer) {
		multiCaster.addObserver(observer);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#getObservers()
	 */
	public List<Observer<GraphSelectionMessage>> getObservers() {
		return multiCaster.getObservers();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.lang.observer.Observable#removeObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void removeObserver(Observer<GraphSelectionMessage> observer) {
		multiCaster.removeObserver(observer);
	}

}
