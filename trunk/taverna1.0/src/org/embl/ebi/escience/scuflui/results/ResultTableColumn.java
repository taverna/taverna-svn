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
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class ResultTableColumn extends ResultTableCellCollection
{
	private Collection sources = new ArrayList();
	private Collection results = new HashSet();
	protected ResultTableColumn previousColumn;

	public void addSource(ResultSource source)
	{
		System.out.println("Adding source: " + source);
		sources.add(source);
	}

	public int fillColumn(int startRow)
	{
		int currentRow = startRow;
		Iterator sourceIterator = sources.iterator();
		while (sourceIterator.hasNext())
		{
			ResultSource source = (ResultSource) sourceIterator.next();
			Iterator resultIterator = source.results.values().iterator();
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

	private ResultTableCell createCell(ResultTableCellCollection parent, ResultThing thing,
										int startRow)
	{
		if (thing.getDataThing().getDataObject() instanceof Collection)
		{
			return createCollection(thing, startRow);
		}

		if (startRow != 0)
		{
			ResultThing previousThing = getValue(startRow - 1);
			if (previousThing != null && previousThing.equals(thing))
			{
				ResultTableCell cell = getCell(startRow - 1);
				extendCell(cell, 1);
				System.out.println("Extended cell " + cell.startRow + "-" + cell.endRow);
				// TODO: Extend other previous cells
				return cell;
			}
		}
		int currentRow = startRow;
		ResultTableCell cell = new ResultTableCell(startRow);
		for (int index = 0; index < thing.inputLSIDs.length; index++)
		{
			ResultTableColumn column = previousColumn;
			while (column != null)
			{
				ResultTableCell inputCell = column.createCell(thing.inputLSIDs[index], currentRow);
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

		add(cell, thing);
		if (parent != this)
		{
			parent.add(cell, thing);
		}
		results.add(thing);
		return cell;
	}

	protected Collection getCellsBetween(int startRow, int endRow)
	{
		Iterator cellIterator = cells.keySet().iterator();
		Collection cellList = new ArrayList();
		while (cellIterator.hasNext())
		{
			ResultTableCell cell = (ResultTableCell) cellIterator.next();
			if ((startRow <= cell.startRow && endRow >= cell.startRow)
					|| (startRow <= cell.endRow && endRow >= cell.endRow))
			{
				cellList.add(cell);
			}
		}
		return cellList;
	}

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

	ResultTableCell createCell(String lsid, int startRow)
	{
		ResultThing thing = getResultThing(lsid);
		if (thing != null)
		{
			return createCell(this, thing, startRow);
		}
		return null;
	}

	public String toString()
	{
		Iterator sourceIterator = sources.iterator();
		String name = null;
		while(sourceIterator.hasNext())
		{
			ResultSource source = (ResultSource)sourceIterator.next();
			if(name == null)
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