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
 */
public class ResultTable extends JTable
{
	public interface TableSelectionListener
	{
		public void valueChanged(ResultTable table, ResultThing thing);
	}

	public class ResultTableCellRenderer extends DefaultTableCellRenderer
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

		public Component getTableCellRendererComponent(JTable table, Object value,
														boolean isSelected, boolean hasFocus,
														int row, int column)
		{
			setIcon(null);
			hasFocus = false;
			boolean root = false;
			boolean leaf = false;
			ResultTableCell selectedCell = ((ResultTable) table).getSelectedCell();
			setFont(table.getFont());

			if (value != null)
			{
				//setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
				// Color.WHITE));
				ResultTableCell cell = ((ResultTable) table).getCell(row, column);
				if (cell != null)
				{
					if (selectedCell != null)
					{
						hasFocus = selectedCell == cell;
						isSelected = (selectedCell.startRow <= cell.startRow && selectedCell.endRow >= cell.startRow)
								|| (selectedCell.startRow <= cell.endRow && selectedCell.endRow >= cell.endRow)
								|| (cell.startRow <= selectedCell.startRow && cell.endRow >= selectedCell.endRow);
					}
					if (!(cell.parent instanceof ResultTableColumn))
					{
						if (cell.parent.getCell(row - 1) == null)
						{
							root = true;
						}
						else
						{
							leaf = true;
						}
					}
				}
				setValue(value);
			}
			else
			{
				if (selectedCell != null)
				{
					isSelected = row >= selectedCell.startRow && row <= selectedCell.endRow;
				}
			}
			
			setBorder(noFocusBorder);
			if (root)
			{
				setIcon(UIManager.getIcon("Tree.expandedIcon"));
			}
			else
			{
				setIcon(null);
			}
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
			if (leaf)
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

	public void addTableSelectionListener(TableSelectionListener listener)
	{
		listeners.add(listener);
	}

	public void removeTableSelectionListener(TableSelectionListener listener)
	{
		listeners.remove(listener);
	}

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

	public void columnSelectionChanged(ListSelectionEvent e)
	{
		super.columnSelectionChanged(e);
		fireSelectionChange();
	}

	public void valueChanged(ListSelectionEvent e)
	{
		super.valueChanged(e);
		fireSelectionChange();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#isCellSelected(int, int)
	 */
	//	public boolean isCellSelected(int row, int column)
	//	{
	//		ResultTableCell cell = (ResultTableCell)getValueAt(row, column);
	//		if(cell == null)
	//		{
	//			return super.isCellSelected(row, column);
	//		}
	//		if(getSelectedColumn() == column)
	//		{
	//			return row >= cell.startRow && row <= cell.endRow;
	//		}
	//		return false;
	//	}
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

	public Rectangle getCellRect(ResultTableCell cell, int column, boolean includeSpacing)
	{
		Rectangle rect = super.getCellRect(cell.startRow, column, includeSpacing);
		Rectangle endRect = super.getCellRect(cell.endRow, column, includeSpacing);
		rect.add(endRect);
		return rect;
	}

	public Rectangle getCellRect(int row, int column, boolean includeSpacing)
	{
		ResultTableCell cell = getCell(row, column);
		if (cell == null)
		{
			return super.getCellRect(row, column, includeSpacing);
		}
		return getCellRect(cell, column, includeSpacing);
	}

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
		setDefaultRenderer(ResultTableCell.class, new ResultTableCellRenderer());
	}
}
