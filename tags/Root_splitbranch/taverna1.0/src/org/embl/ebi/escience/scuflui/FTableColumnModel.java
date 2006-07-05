package org.embl.ebi.escience.scuflui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The table model for a FacetsTable. This encapsulates which columns are
 * present in the table,
 * 
 * @author Matthew Pocock
 */
public class FTableColumnModel {
	private static final Logger LOG = Logger.getLogger(FTableColumnModel.class);

	private final List listeners;

	private final PropertyChangeListener changeForwarder;

	private final List columns;

	public FTableColumnModel() {
		this.listeners = new ArrayList();
		this.changeForwarder = new ChangeForwarder();
		this.columns = new ArrayList();
	}

	public void addColumn(FTableColumn col) {
		columns.add(col);
		col.addPropertyChangeListener(changeForwarder);
		fireColumnAdded(columns.size() - 1);
	}

	public void addColumn(int to, FTableColumn col) {
		columns.add(to, col);
		col.addPropertyChangeListener(changeForwarder);
		fireColumnAdded(to);
	}

	public void removeColumn(FTableColumn col) {
		col.removePropertyChangeListener(changeForwarder);
		int from = columns.indexOf(col);
		columns.remove(col);
		fireColumnRemoved(from);
	}

	public void removeColumn(int indx) {
		columns.remove(indx);
		fireColumnRemoved(indx);
	}

	public void moveColumn(int from, int to) {
		FTableColumn col = (FTableColumn) columns.remove(from);
		columns.add(to, col);
		fireColumnMoved(from, to);
	}

	public void addFTableColumnModelListener(FTableColumnModelListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeFTableColumnModelListener(
			FTableColumnModelListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public Iterator columnIterator() {
		return columns.iterator();
	}

	public FTableColumn getColumn(int indx) {
		return (FTableColumn) columns.get(indx);
	}

	public int getColumnCount() {
		return columns.size();
	}

	public int getColumnIndex(FTableColumn col) {
		return columns.indexOf(col);
	}

	protected void fireColumnAdded(int to) {
		FTableColumnModelEvent evt = new FTableColumnModelEvent(this, -1, to,
				null);
		List local;
		synchronized (listeners) {
			local = new ArrayList(listeners);
		}

		for (Iterator i = local.iterator(); i.hasNext();) {
			FTableColumnModelListener l = (FTableColumnModelListener) i.next();
			l.columnAdded(evt);
		}
	}

	protected void fireColumnRemoved(int from) {
		FTableColumnModelEvent evt = new FTableColumnModelEvent(this, from, -1,
				null);
		List local;
		synchronized (listeners) {
			local = new ArrayList(listeners);
		}

		for (Iterator i = local.iterator(); i.hasNext();) {
			FTableColumnModelListener l = (FTableColumnModelListener) i.next();
			l.columnRemoved(evt);
		}
	}

	protected void fireColumnMoved(int from, int to) {
		FTableColumnModelEvent evt = new FTableColumnModelEvent(this, from, to,
				null);
		List local;
		synchronized (listeners) {
			local = new ArrayList(listeners);
		}

		for (Iterator i = local.iterator(); i.hasNext();) {
			FTableColumnModelListener l = (FTableColumnModelListener) i.next();
			l.columnMoved(evt);
		}
	}

	protected void fireColumnChanged(PropertyChangeEvent pce) {
		FTableColumnModelEvent evt = new FTableColumnModelEvent(this, -1,
				columns.indexOf(pce.getSource()), pce);
		LOG.info("PCE: " + evt);
		if (evt.getToIndex() == -1) {
			LOG.info("The source object may be wrong");
		}
		List local;
		synchronized (listeners) {
			local = new ArrayList(listeners);
		}

		for (Iterator i = local.iterator(); i.hasNext();) {
			FTableColumnModelListener l = (FTableColumnModelListener) i.next();
			l.columnChanged(evt);
		}
	}

	private class ChangeForwarder implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			fireColumnChanged(evt);
		}
	}
}
