/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.embl.ebi.escience.baclava.DataThing;

public class ResultTableCompositeCell extends ResultTableCell
{
	protected ArrayList cells = new ArrayList();

	protected ResultTableCompositeCell(ResultTableModel model, DataThing thing)
	{
		// Constructor for column
		super(model, thing);
	}

	/**
	 * @param model
	 * @param parent
	 * @param thing
	 * @param startRow
	 * @param endRow
	 */
	public ResultTableCompositeCell(ResultTableModel model, ResultTableCompositeCell parent, DataThing thing, int startRow,
									int endRow)
	{
		super(model, parent, thing, startRow, endRow);

	}

	public void add(ResultTableCell cell)
	{
		cells.add(cell);
		endRow = Math.max(endRow, cell.endRow);
	}

	public ResultTableCell getCell(int row)
	{
		for (int index = 0; index < cells.size(); index++)
		{
			ResultTableCell cell = (ResultTableCell) cells.get(index);
			if (cell.startRow <= row && cell.endRow >= row)
			{
				if (cell instanceof ResultTableCompositeCell)
				{
					return ((ResultTableCompositeCell) cell).getCell(row);
				}
				return cell;
			}
		}
		return null;
	}

	public ResultTableCell createCell(DataThing thing, int startRow, Collection inputLSIDs)
	{
		HashSet inputList = new HashSet();
		if (inputLSIDs != null)
		{
			inputList.addAll(inputLSIDs);
		}
		model.getAllInputs(thing, inputList);
		int currentRow = startRow;

		if (thing.getDataObject() instanceof List)
		{
			Iterator children = thing.childIterator();
			ResultTableCompositeCell parent = new ResultTableCompositeCell(model, this, thing, startRow,
					startRow);
			while (children.hasNext())
			{
				DataThing child = (DataThing) children.next();
				ResultTableCell cell = parent.createCell(child, currentRow, inputList);
				if (cell != null)
				{
					currentRow = cell.endRow + 1;
				}
			}
			System.out.println("Parent created: " + thing.getLSID(thing.getDataObject()) + ": "
					+ parent.startRow + "-" + parent.endRow);
			add(parent);
			return parent;
		}

		ResultTableCell cell = null;
		if (startRow != 0)
		{
			ResultTableCell previousCell = getColumn().getCell(startRow - 1);
			if (previousCell != null && previousCell.thing.equals(thing))
			{
				previousCell.endRow = currentRow;
				System.out.println("Cell extended "
						+ previousCell.thing.getLSID(previousCell.thing.getDataObject()) + ": "
						+ previousCell.startRow + "-" + previousCell.endRow);
				// TODO: extend input cells?
				cell = previousCell;
			}
		}
		if (cell == null)
		{
			cell = new ResultTableCell(model, this, thing, startRow, currentRow);
			cell.createInputCells(inputList);
			System.out.println("Cell created: " + thing.getLSID(thing.getDataObject()) + ": "
					+ cell.startRow + "-" + cell.endRow);
		}
		add(cell);
		return cell;
	}
}