/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.6 $
 */
public class ResultTableColumn extends ResultTableCellCollection
{
	private Collection sources = new ArrayList();
	private Collection results = new HashSet();
	protected ResultTableColumn previousColumn;
	protected ResultTableColumn nextColumn;

	/**
	 * 
	 * @param source
	 */
	public void addSource(ResultSource source)
	{
		sources.add(source);
	}

	/**
	 * Fills this column with results. Iterates through all the
	 * {@link ResultSource ResultSources}displayed in this column and then adds
	 * the all outputs from each of them, assuming that each output doesn't
	 * already appear in the column.
	 * 
	 * @param startRow
	 *            the row to start adding the results to
	 * @return the row after the last added result
	 */
	public int fillColumn(int startRow)
	{
		int currentRow = startRow;
		Iterator sourceIterator = sources.iterator();
		while (sourceIterator.hasNext())
		{
			ResultSource source = (ResultSource) sourceIterator.next();
			Iterator resultIterator = source.results();
			while (resultIterator.hasNext())
			{
				ResultThing result = (ResultThing) resultIterator.next();
				if (!results.contains(result)
						&& !(result.getDataThing().getDataObject() instanceof Collection))
				{
					ResultTableCell cell = createCell(this, result, currentRow);
					currentRow = cell.endRow + 1;
				}
			}
		}
		return currentRow;
	}

	/**
	 * 
	 * @param thing
	 * @param startRow
	 * @return a 
	 */
	private ResultTableCell createCollection(ResultThing thing, int startRow)
	{
		int currentRow = startRow;
		Iterator children = thing.getDataThing().childIterator();
		ResultTableCellCollection collection = new ResultTableCellCollection();
		while (children.hasNext())
		{
			DataThing child = (DataThing) children.next();
			ResultThing childResult = getResultThing(child.getLSID(child.getDataObject()));
			if (childResult != null)
			{
				ResultTableCell cell = createCell(collection, childResult, currentRow);
				currentRow = cell.endRow + 1;
			}
		}
		ResultTableCell cell = new ResultTableCell(startRow);
		cell.endRow = currentRow - 1;
		return cell;
	}

	/**
	 * This creates a ResultTableCell for the specified
	 * {@link ResultThing result}.
	 * <p>
	 * TODO Extend this method to allow the table to be aid out from different
	 * columns. This will probably require this method to have parameters to
	 * determine what direction it should go to construct input/output cells.
	 * </p>
	 * 
	 * @param parent
	 * @param result
	 * @param startRow
	 * @return the ResultTableCell created
	 */
	private ResultTableCell createCell(ResultTableCellCollection parent, ResultThing result,
										int startRow)
	{
		if (result.getDataThing().getDataObject() instanceof Collection)
		{
			return createCollection(result, startRow);
		}

		if (startRow != 0)
		{
			ResultThing previousThing = getValue(startRow - 1);
			if (previousThing != null && previousThing.equals(result))
			{
				ResultTableCell cell = getCell(startRow - 1);
				extendCell(cell, 1);
				return cell;
			}
		}
		int currentRow = startRow;
		ResultTableCell cell = new ResultTableCell(startRow);
		for (int index = 0; index < result.inputLSIDs.length; index++)
		{
			ResultTableColumn column = previousColumn;
			while (column != null)
			{
				ResultTableCell inputCell = column.createCell(result.inputLSIDs[index], currentRow);
				if (inputCell != null)
				{
					currentRow = inputCell.endRow + 1;
					break;
				}
				column = column.previousColumn;
			}
		}
		if (currentRow != startRow)
		{
			cell.endRow = currentRow - 1;
		}
		else
		{
			cell.endRow = startRow;
		}

		add(cell, result);
		if (parent != this)
		{
			parent.add(cell, result);
		}
		results.add(result);
		return cell;
	}

	/**
	 * This iterates through all of the cells in the column, and returns a
	 * collection of all the cells between the two given rows, including cells
	 * which are only on either the <code>startRow</code> or the
	 * <code>endRow</code>.
	 * 
	 * @param startRow
	 * @param endRow
	 * @return a <code>Collection</code> of all the cells between the two
	 *         given rows
	 */
	protected Collection getCellsBetween(int startRow, int endRow)
	{
		Iterator cellIterator = cells.keySet().iterator();
		Collection cellList = new ArrayList();
		while (cellIterator.hasNext())
		{
			ResultTableCell cell = (ResultTableCell) cellIterator.next();
			if ((startRow <= cell.startRow && endRow >= cell.startRow)
					|| (startRow <= cell.endRow && endRow >= cell.endRow)
					|| (cell.startRow <= startRow && cell.endRow >= endRow))
			{
				cellList.add(cell);
			}
		}
		return cellList;
	}

	/**
	 * Increases the height of a {@link ResultTableCell cell}and then
	 * recalculates the heights of the inputs the inputs cells. It divides the
	 * new height of the cell equally amongst all of its input cells.
	 * <p>
	 * TODO Really, when a cell is extended, both the inputs and the outputs
	 * should be re-evaluated, with height and position re-examined. Currently,
	 * this method sometimes doesn't produce the better layout. Of course, if
	 * you really wanted an ideal layout, you'd have to try switching round the
	 * order of input cells for every cell and see which combinations gain you
	 * the most.
	 * </p>
	 * 
	 * @param cell
	 *            the cell to increase the height of
	 * @param amount
	 *            the amount to increase the height of the cell by
	 */
	private void extendCell(ResultTableCell cell, int amount)
	{
		cell.endRow += amount;
		if (previousColumn != null)
		{
			Collection inputCells = previousColumn.getCellsBetween(cell.startRow, cell.endRow);
			if (inputCells.size() != 0)
			{
				int height = (cell.endRow - cell.startRow + 1) / inputCells.size();
				Iterator cellIterator = inputCells.iterator();
				while (cellIterator.hasNext())
				{
					ResultTableCell inputCell = (ResultTableCell) cellIterator.next();
					int startRow = Math.max(cell.startRow, inputCell.startRow);
					int currentHeight = inputCell.endRow - startRow + 1;
					if (currentHeight < height)
					{
						previousColumn.extendCell(inputCell, height - currentHeight);
					}
				}
			}
		}
	}

	/**
	 * Returns a {@link ResultThing result}by its lsid. This iterates through
	 * all the {@link ResultSource ResultSources}of the column, calling
	 * {@link ResultSource#getResultThing(String) getResultThing(lsid)}on each.
	 * 
	 * @param lsid
	 *            the lsid of a workflow output
	 * @return the <code>ResultThing</code> corresponding to the given lsid
	 */
	private ResultThing getResultThing(String lsid)
	{
		Iterator sourceIterator = sources.iterator();
		while (sourceIterator.hasNext())
		{
			ResultSource source = (ResultSource) sourceIterator.next();
			ResultThing thing = source.getResultThing(lsid);
			if (thing != null)
			{
				return thing;
			}
		}
		return null;
	}

	/**
	 * Creates a {@link ResultTableCell cell}from an lsid. This just calls
	 * {@link #createCell(ResultTableCellCollection, ResultThing, int)}, after
	 * getting the ResultThing for the given lsid.
	 * 
	 * @param lsid
	 *            the lsid of the result
	 * @param startRow
	 * @return a new <code>ResultTableCell</code>
	 */
	ResultTableCell createCell(String lsid, int startRow)
	{
		ResultThing thing = getResultThing(lsid);
		if (thing != null)
		{
			return createCell(this, thing, startRow);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		Iterator sourceIterator = sources.iterator();
		String name = null;
		while (sourceIterator.hasNext())
		{
			ResultSource source = (ResultSource) sourceIterator.next();
			if (name == null)
			{
				name = source.toString();
			}
			else
			{
				name = name + ", " + source.toString();
			}
		}
		if (previousColumn == null)
		{
			name = "Inputs: " + name;
		}

		return name;
	}
}