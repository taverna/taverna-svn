/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;


public class ResultTableCell
{
	protected ResultTableModel model;
	DataThing thing;
	protected int startRow = 0;
	protected int endRow = 0;
	protected ResultTableCompositeCell parent;

	protected Collection inputs = new HashSet();
	protected Collection outputs = new HashSet();

	protected ResultTableCell(ResultTableModel model, DataThing thing)
	{
		this.thing = thing;
		this.model = model;
	}

	public ResultTableCell(ResultTableModel model, ResultTableCompositeCell parent, DataThing thing, int startRow,
							int endRow)
	{
		this(model, thing);
		this.parent = parent;
		this.startRow = startRow;
		this.endRow = endRow;
	}

	public DataThing getDataThing()
	{
		return thing;
	}

	public void addInput(ResultTableCell cell)
	{
		if (!inputs.contains(cell))
		{
			inputs.add(cell);
		}
	}

	public void addOutput(ResultTableCell cell)
	{
		if (!outputs.contains(cell))
		{
			outputs.add(cell);
		}
	}

	public boolean hasOutputs()
	{
		return outputs.size() != 0;
	}

	public ResultTableColumn getColumn()
	{
		return parent.getColumn();
	}

	/**
	 * @param inputList
	 */
	public void createInputCells(Collection inputList)
	{
		int currentRow = startRow;
		Iterator inputColumns = getColumn().inputs.iterator();
		while(inputColumns.hasNext())
		{
			ResultTableColumn column = (ResultTableColumn) inputColumns.next();
			Iterator lsidIterator = inputList.iterator();
			while(lsidIterator.hasNext())
			{
				String lsid = (String)lsidIterator.next();
				HashSet inputLSIDs = new HashSet();
				DataThing thing = model.findThing(column.getDataThing(), lsid, inputLSIDs);
				if (thing != null)
				{
					ResultTableCell cell = column.createCell(thing, currentRow, inputLSIDs);
					if (cell != null)
					{
						currentRow = cell.endRow + 1;
						endRow = Math.max(endRow, cell.endRow);
					}
					addInput(cell);
				}
			}
		}
	}
}