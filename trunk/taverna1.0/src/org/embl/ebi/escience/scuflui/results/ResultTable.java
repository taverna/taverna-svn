/*
 * Created on Sep 14, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

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
				setBackground(new Color(250,252,255));
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
//				int topBorder = 0;
				int bottomBorder = 0;
//				if(cell.startRow == row && row != 0)
//				{
//					topBorder = 1;
//				}
				if(cell.endRow == row)
				{
					bottomBorder = 1;
				}
				setBorder(BorderFactory.createMatteBorder(0,0,bottomBorder,0, new Color(235, 235, 235)));
				if(cell.getColumn().hasOutputs() && !(cell.parent instanceof ResultTableColumn))
				{
					// List!
					if(cell.startRow == cell.parent.startRow)
					{
						setIcon(UIManager.getIcon("Tree.expandedIcon"));
					}
					else
					{
						setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,16,0,0), getBorder()));
					}
				}
				if(hasFocus)
				{
					setBackground(new Color(225,235,245));
				}
				setValue(cell.thing.getDataObject());
			}
			return this;
		}
	}

	public ResultTable(ScuflModel model, WorkflowInstance workflowInstance)
	{
		setModel(new ResultTableModel(model, workflowInstance));
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
		setGridColor(new Color(235, 235, 235));
		setSelectionBackground(new Color(232, 242, 254));
		setSelectionForeground(Color.BLACK);
		setDefaultRenderer(ResultTableCell.class, new ResultTableCellRenderer());
	}
}
