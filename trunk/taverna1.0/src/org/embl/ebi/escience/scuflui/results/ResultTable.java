/*
 * Created on Sep 14, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class ResultTable extends JTable
{
	public class ResultTableCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value,
														boolean isSelected, boolean hasFocus,
														int row, int column)
		{
			setIcon(null);
			if(value != null)
			{
				setBackground(new Color(232, 242, 254));
			}
			else
			{
				setBackground(Color.WHITE);
			}
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
												column);
			if(value != null)
			{
				ResultTableCell cell = (ResultTableCell)value;
				setBorder(BorderFactory.createMatteBorder(1,0,1,0, Color.WHITE));
				if(hasFocus)
				{
					setBackground(new Color(202, 222, 254));
				}				
				if(cell.getColumn().hasOutputs() && !(cell.parent instanceof ResultTableColumn))
				{
					// List!
					if(cell.startRow == cell.parent.startRow)
					{
						setIcon(UIManager.getIcon("Tree.expandedIcon"));
					}
					else
					{
						setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,12,0,0, getBackground()), getBorder()));
					}
				}
				setValue(cell.thing.getDataObject());
			}

			return this;
		}
	}

	public Rectangle getCellRect(int row, int column, boolean includeSpacing)
	{
		ResultTableCell cell = (ResultTableCell)getValueAt(row, column);		
		if(cell == null)
		{
			return super.getCellRect(row, column, includeSpacing);
		}
		Rectangle rect = super.getCellRect(cell.startRow, column, includeSpacing);
		Rectangle endRect = super.getCellRect(cell.endRow, column, includeSpacing);
		rect.add(endRect);
		return rect;
	}
	
	public ResultTable(ScuflModel model, WorkflowInstance workflowInstance)
	{
		setModel(new ResultTableModel(model, workflowInstance));
		setUI(new ResultTableUI());
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
		setGridColor(new Color(235, 235, 235));
		setSelectionBackground(new Color(232, 242, 254));
		setSelectionForeground(Color.BLACK);
		setDefaultRenderer(ResultTableCell.class, new ResultTableCellRenderer());
	}
}
