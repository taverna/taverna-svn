/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.HashMap;
import java.util.Iterator;

public class ResultTableCellCollection
{
	protected HashMap cells = new HashMap();
	protected boolean expanded = true;

	public void add(ResultTableCell cell, ResultThing thing)
	{
		cells.put(cell, thing);
		cell.parent = this;
		System.out.println("Adding cell " + cell.startRow + "-" + cell.endRow);
	}

	public ResultTableCell getCell(int row)
	{
		Iterator cellIterator = cells.keySet().iterator();
		while(cellIterator.hasNext())
		{
			ResultTableCell cell = (ResultTableCell) cellIterator.next();
			if (cell.startRow <= row && cell.endRow >= row)
			{
				return cell;
			}
		}
		return null;
	}
	
	public ResultThing getValue(int row)
	{
		return (ResultThing)cells.get(getCell(row));
	}
}