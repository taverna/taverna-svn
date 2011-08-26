package org.embl.ebi.escience.scuflui.facets;


/**
 * Listener interace for things interested in table models changing.
 * 
 * @author Matthew Pocock
 */
public interface FTableColumnModelListener extends java.util.EventListener {
	public void columnAdded(FTableColumnModelEvent evt);

	public void columnRemoved(FTableColumnModelEvent evt);

	public void columnMoved(FTableColumnModelEvent evt);

	public void columnChanged(FTableColumnModelEvent evt);
}
