/*
 * Created on Sep 23, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A special TableUI, to render the ResultTable with cells that can span several
 * rows. Its mostly code borrowed straight from BasicTableUI, but since most of
 * the important methods there are marked as private, I have to reproduce them
 * here. The great benefits of inheritance, eh?
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 */
public class ResultTableUI extends BasicTableUI
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics,
	 *      javax.swing.JComponent)
	 */
	public void paint(Graphics g, JComponent c)
	{
		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0)
		{
			return;
		}
		Rectangle clip = g.getClipBounds();
		Point upperLeft = clip.getLocation();
		Point lowerRight = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
		int rMin = table.rowAtPoint(upperLeft);
		int rMax = table.rowAtPoint(lowerRight);
		// This should never happen.
		if (rMin == -1)
		{
			rMin = 0;
		}
		// If the table does not have enough rows to fill the view we'll get -1.
		// Replace this with the index of the last row.
		if (rMax == -1)
		{
			rMax = table.getRowCount() - 1;
		}

		boolean ltr = table.getComponentOrientation().isLeftToRight();
		int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight);
		int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);
		// This should never happen.
		if (cMin == -1)
		{
			cMin = 0;
		}
		// If the table does not have enough columns to fill the view we'll get
		// -1.
		// Replace this with the index of the last column.
		if (cMax == -1)
		{
			cMax = table.getColumnCount() - 1;
		}

		// Paint the grid. Removed column parameters since I don't use them. I
		// don't draw any vertical grid lines.
		paintGrid(g, rMin, rMax);

		// Paint the cells.
		paintCells(g, rMin, rMax, cMin, cMax);
	}

	/**
	 * Paints the horizontal grid lines between rMin and rMax if
	 * {@link JTable#getShowHorizontalLines() getShowHorizontalLines}returns
	 * <code>true</code>.
	 * 
	 * @param g
	 *            the graphics context to draw onto
	 * @param rMin
	 *            the first row to draw the grid around
	 * @param rMax
	 *            the last row to draw the grid around
	 */
	private void paintGrid(Graphics g, int rMin, int rMax)
	{
		if (table.getShowHorizontalLines())
		{
			g.setColor(table.getGridColor());

			int rowHeight = table.getRowHeight();
			int tableWidth = g.getClipBounds().width;
			int x = g.getClipBounds().x;
			int y = rowHeight * rMin;
			for (int row = rMin; row <= rMax; row++)
			{
				y += rowHeight;
				g.drawLine(x, y - 1, tableWidth - 1, y - 1);
			}
		}
	}

	/**
	 * Paints the cells into the table. This is really where all the real
	 * changes from BasicTableUI are really. Rather than iterating over every
	 * row, this calls
	 * {@link ResultTableColumn#getCellsBetween(int,int) getCellsBetween(rMin, rMax)}
	 * and iterates over each cell in that. This ensures that each cell is only
	 * draw once. The downside is that it completely ignores any rows without a
	 * {@link ResultTableCell ResultTableCell}, so you can't do anything with
	 * empty cells.
	 * 
	 * @param g
	 *            the graphics context to draw onto
	 * @param rMin
	 *            the first row to draw the cells of
	 * @param rMax
	 *            the last row to draw the cells of
	 * @param cMin
	 *            the first column to draw the cells of
	 * @param cMax
	 *            the last column to draw the cells of
	 */
	private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax)
	{
		JTableHeader header = table.getTableHeader();
		TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

		for (int column = cMin; column <= cMax; column++)
		{
			ResultTableColumn aColumn = ((ResultTableModel) table.getModel()).getColumn(column);
			Iterator cells = aColumn.getCellsBetween(rMin, rMax).iterator();
			while (cells.hasNext())
			{
				ResultTableCell cell = (ResultTableCell) cells.next();
				Rectangle cellRect = ((ResultTable) table).getCellRect(cell, column, false);
				paintCell(g, cellRect, cell.startRow, column);
			}
		}
		// Paint the dragged column if we are dragging.
		if (draggedColumn != null)
		{
			paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance());
		}

		// Remove any renderers that may be left in the rendererPane.
		rendererPane.removeAll();
	}

	/**
	 * Paints an individual cell.
	 * 
	 * @param g
	 * @param cellRect
	 * @param row
	 * @param column
	 */
	private void paintCell(Graphics g, Rectangle cellRect, int row, int column)
	{
		TableCellRenderer renderer = table.getCellRenderer(row, column);
		Component component = table.prepareRenderer(renderer, row, column);
		rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y, cellRect.width,
									cellRect.height, true);
	}

	/**
	 * Lifted straight from BasicTableUI...
	 * 
	 * @param g
	 * @param rMin
	 * @param rMax
	 * @param draggedColumn
	 * @param distance
	 */
	private void paintDraggedArea(Graphics g, int rMin, int rMax, TableColumn draggedColumn,
									int distance)
	{
		int draggedColumnIndex = viewIndexForColumn(draggedColumn);

		Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
		Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

		Rectangle vacatedColumnRect = minCell.union(maxCell);

		// Paint a gray well in place of the moving column.
		g.setColor(table.getParent().getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width,
					vacatedColumnRect.height);

		// Move to the where the cell has been dragged.
		vacatedColumnRect.x += distance;

		// Fill the background.
		g.setColor(table.getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y, vacatedColumnRect.width,
					vacatedColumnRect.height);

		// Paint the vertical grid lines if necessary.
		if (table.getShowVerticalLines())
		{
			g.setColor(table.getGridColor());
			int x1 = vacatedColumnRect.x;
			int y1 = vacatedColumnRect.y;
			int x2 = x1 + vacatedColumnRect.width - 1;
			int y2 = y1 + vacatedColumnRect.height - 1;
			// Left
			g.drawLine(x1 - 1, y1, x1 - 1, y2);
			// Right
			g.drawLine(x2, y1, x2, y2);
		}

		for (int row = rMin; row <= rMax; row++)
		{
			// Render the cell value
			Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
			r.x += distance;
			paintCell(g, r, row, draggedColumnIndex);

			// Paint the (lower) horizontal grid line if necessary.
			if (table.getShowHorizontalLines())
			{
				g.setColor(table.getGridColor());
				Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
				rcr.x += distance;
				int x1 = rcr.x;
				int y1 = rcr.y;
				int x2 = x1 + rcr.width - 1;
				int y2 = y1 + rcr.height - 1;
				g.drawLine(x1, y2, x2, y2);
			}
		}
	}

	/**
	 * @param aColumn
	 * @return index of the view order of the column
	 */
	private int viewIndexForColumn(TableColumn aColumn)
	{
		TableColumnModel cm = table.getColumnModel();
		for (int column = 0; column < cm.getColumnCount(); column++)
		{
			if (cm.getColumn(column) == aColumn)
			{
				return column;
			}
		}
		return -1;
	}
}