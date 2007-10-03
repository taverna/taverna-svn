/*
 * Created on Sep 14, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 */
public class ResultTable extends JTable
{
	/**
	 * Interface to recieve notifications when the table selection changes
	 */
	public interface TableSelectionListener
	{
		/**
		 * @param table
		 *            the {@link ResultTable table}that generated the event
		 * @param thing
		 *            the newly selected result
		 */
		public void valueChanged(ResultTable table, ResultThing thing);
	}

	/**
	 * The cell renderer for the result table. Currently only renders cells as
	 * text.
	 */
	private class ResultTableCellRenderer extends DefaultTableCellRenderer
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Component#isOpaque()
		 */
		public boolean isOpaque()
		{
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
														boolean isSelected, boolean hasFocus,
														int row, int column)
		{
			setIcon(null);
			hasFocus = false;
			boolean listRoot = false;
			boolean listLeaf = false;

			if (value != null)
			{
				ResultTableCell selectedCell = ((ResultTable) table).getSelectedCell();
				ResultTableCell cell = ((ResultTable) table).getCell(row, column);
				if (cell != null)
				{
					if (selectedCell != null)
					{
						// Check whether the selectedCell is the current one and
						// set focus to true if so. We should also at this point
						// set selected to true if any of the peer cells in that
						// column have the same DataThing
						ResultThing selectedResult = (ResultThing) getValueAt(getSelectedRow(),
																				getSelectedColumn());
						hasFocus = (value == selectedResult);

						// Really want to iterate over this and set selected to
						// be true if the constraint applies to any cell with
						// the same datathing as the primary one.
						if (!hasFocus)
						{
							for (int i = 0; i < getModel().getRowCount() && isSelected == false; i++)
							{
								ResultThing currentResult = (ResultThing) getValueAt(
																						i,
																						getSelectedColumn());
								if (currentResult != null && currentResult == selectedResult)
								{
									ResultTableCell currentCell = ((ResultTable) table)
											.getCell(i, getSelectedColumn());
									// Urg. This is an ugly statement. But
									// basically
									// it says that all cells that occupy the
									// same
									// rows as the selected cell should be
									// highlighted. This should highlight all
									// the
									// results which are either inputs or
									// outputs of
									// this one, but in a few rare cases will
									// actually miss out a cell if it is on a
									// different row. Really we should do some
									// graph
									// traversal here to work out what cell
									// should
									// be highlighted.
									isSelected = (currentCell.startRow <= cell.startRow && currentCell.endRow >= cell.startRow)
											|| (currentCell.startRow <= cell.endRow && currentCell.endRow >= cell.endRow)
											|| (cell.startRow <= currentCell.startRow && cell.endRow >= currentCell.endRow);
								}
							}
						}
					}

					if (!(cell.parent instanceof ResultTableColumn))
					{
						if (cell.parent.getCell(row - 1) == null)
						{
							listRoot = true;
						}
						else
						{
							listLeaf = true;
						}
					}
				}
				setValue(value);
			}
			else
			{
				// Shouldn't actually get here. The ResultTableUI iterates over
				// each ResultTableCell object in a column, only drawing those.
				// Cells without a ResultTableCell (and hence a value) are
				// ignored...
			}

			setFont(table.getFont());
			setBorder(noFocusBorder);
			if (isSelected)
			{
				super.setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			}
			else
			{
				super.setForeground(table.getForeground());
				super.setBackground(Color.WHITE);
			}
			if (hasFocus)
			{
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				setBackground(new Color(202, 222, 254));
				if (table.isCellEditable(row, column))
				{
					super.setForeground(UIManager.getColor("Table.focusCellForeground"));
					super.setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			}

			if (listRoot)
			{
				setIcon(UIManager.getIcon("Tree.expandedIcon"));
			}
			else
			{
				setIcon(null);
			}
			if (listLeaf)
			{
				setBorder(BorderFactory.createCompoundBorder(BorderFactory
						.createMatteBorder(0, 12, 0, 0, getBackground()), getBorder()));
			}
			return this;
		}
	}

	private Collection listeners = new HashSet();

	private ResultThing selectedResult = null;
	private ResultTableCell selectedCell = null;

	/**
	 * @param model
	 * @param workflowInstance
	 */
	public ResultTable(ScuflModel model, WorkflowInstance workflowInstance)
	{
		setModel(new ResultTableModel(model, workflowInstance));
		setUI(new ResultTableUI());
		setShowGrid(true);
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 1));
		setGridColor(new Color(235, 235, 235));
		setSelectionBackground(new Color(232, 242, 254));
		setSelectionForeground(Color.BLACK);
		setBackground(new Color(235, 235, 235));
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		getTableHeader().setReorderingAllowed(false);
		setDefaultRenderer(ResultTableCell.class, new ResultTableCellRenderer());
	}

	/**
	 * Shockingly, this adds a
	 * {@link ResultTable.TableSelectionListener TableSelectionListener} to this
	 * table.
	 * 
	 * @param listener
	 *            the TableSelectionListener to add
	 */
	public void addTableSelectionListener(TableSelectionListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Removes the given
	 * {@link ResultTable.TableSelectionListener TableSelectionListener} from
	 * this table.
	 * 
	 * @param listener
	 *            the TableSelectionListener to remove
	 */
	public void removeTableSelectionListener(TableSelectionListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @return the selected ResultTableCell
	 * @see ResultTableCell
	 */
	public ResultTableCell getSelectedCell()
	{
		return selectedCell;
	}

	private void fireSelectionChange()
	{
		if ((getModel() instanceof ResultTableModel))
		{
			ResultTableCell cell = getCell(getSelectedRow(), getSelectedColumn());
			if (cell != selectedCell)
			{
				selectedResult = (ResultThing) getValueAt(getSelectedRow(), getSelectedColumn());
				selectedCell = cell;
				Iterator listenerIterator = listeners.iterator();
				while (listenerIterator.hasNext())
				{
					TableSelectionListener listener = (TableSelectionListener) listenerIterator
							.next();
					listener.valueChanged(this, selectedResult);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void columnSelectionChanged(ListSelectionEvent e)
	{
		super.columnSelectionChanged(e);
		fireSelectionChange();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		super.valueChanged(e);
		fireSelectionChange();
	}

	/**
	 * @param row
	 * @param column
	 * @return the <code>ResultTableCell</code> at the specified table
	 *         location
	 * @see ResultTableCell
	 */
	public ResultTableCell getCell(int row, int column)
	{
		try
		{
			return ((ResultTableModel) getModel()).getColumn(column).getCell(row);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * 
	 * 
	 * TODO Extend this to allow column spanning?
	 * @param cell
	 * @param column
	 * @param includeSpacing
	 * @return the bounding rectangle of the specified cell
	 */
	protected Rectangle getCellRect(ResultTableCell cell, int column, boolean includeSpacing)
	{
		Rectangle rect = super.getCellRect(cell.startRow, column, includeSpacing);
		Rectangle endRect = super.getCellRect(cell.endRow, column, includeSpacing);
		rect.add(endRect);
		return rect;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#getCellRect(int, int, boolean)
	 */
	public Rectangle getCellRect(int row, int column, boolean includeSpacing)
	{
		ResultTableCell cell = getCell(row, column);
		if (cell == null)
		{
			return super.getCellRect(row, column, includeSpacing);
		}
		return getCellRect(cell, column, includeSpacing);
	}
}
